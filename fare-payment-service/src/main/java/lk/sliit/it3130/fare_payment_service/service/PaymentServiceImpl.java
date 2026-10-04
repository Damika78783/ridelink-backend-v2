package lk.sliit.it3130.fare_payment_service.service;

import lk.sliit.it3130.fare_payment_service.client.RideClient;
import lk.sliit.it3130.fare_payment_service.config.FareProperties;
import lk.sliit.it3130.fare_payment_service.dto.*;
import lk.sliit.it3130.fare_payment_service.exception.DuplicatePaymentException;
import lk.sliit.it3130.fare_payment_service.exception.PaymentFailedException;
import lk.sliit.it3130.fare_payment_service.exception.ResourceNotFoundException;
import lk.sliit.it3130.fare_payment_service.exception.RideNotCompletedException;
import lk.sliit.it3130.fare_payment_service.model.Payment;
import lk.sliit.it3130.fare_payment_service.model.PaymentStatus;
import lk.sliit.it3130.fare_payment_service.model.VehicleType;
import lk.sliit.it3130.fare_payment_service.repository.PaymentRepository;
import lk.sliit.it3130.fare_payment_service.service.gateway.PaymentGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RideClient rideClient;
    private final FareService fareService;
    private final PaymentGateway paymentGateway;
    private final FareProperties fareProperties;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              RideClient rideClient,
                              FareService fareService,
                              PaymentGateway paymentGateway,
                              FareProperties fareProperties) {
        this.paymentRepository = paymentRepository;
        this.rideClient = rideClient;
        this.fareService = fareService;
        this.paymentGateway = paymentGateway;
        this.fareProperties = fareProperties;
    }

    @Override
    @Transactional(noRollbackFor = PaymentFailedException.class)
    public PaymentResponse processPayment(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request cannot be null");
        }

        // 1. Confirm ride existence and COMPLETED state via Ride Management Service
        RideResponse ride = rideClient.getRideById(request.getRideId());
        if (ride == null) {
            throw new ResourceNotFoundException("Ride not found with id: " + request.getRideId());
        }

        if (!"COMPLETED".equalsIgnoreCase(ride.getStatus())) {
            throw new RideNotCompletedException("Cannot process payment. Ride " + request.getRideId()
                    + " is in status '" + ride.getStatus() + "'. Only COMPLETED rides can be paid.");
        }

        // 2. Reject duplicate successful payment for the same ride
        if (paymentRepository.existsByRideIdAndStatus(request.getRideId(), PaymentStatus.COMPLETED)) {
            throw new DuplicatePaymentException("A successful payment has already been recorded for ride id: "
                    + request.getRideId());
        }

        // 3. Calculate final fare based on ride metrics
        VehicleType vehicleType = ride.getVehicleType() != null ? ride.getVehicleType() : VehicleType.CAR;

        double distanceKm = (ride.getDistanceKm() != null && ride.getDistanceKm() > 0)
                ? ride.getDistanceKm()
                : ((ride.getPickupLatitude() != null && ride.getDropoffLatitude() != null)
                ? fareService.calculateDistance(ride.getPickupLatitude(), ride.getPickupLongitude(),
                ride.getDropoffLatitude(), ride.getDropoffLongitude())
                : 5.0);

        double durationMin = (ride.getDurationMinutes() != null && ride.getDurationMinutes() > 0)
                ? ride.getDurationMinutes()
                : fareService.estimateDurationMinutes(distanceKm);

        FareCalculationResult fareResult = fareService.calculateFare(distanceKm, durationMin, vehicleType);

        // 4. Execute transaction via Payment Gateway (simulates card token '0000' decline)
        GatewayTransactionResult gatewayResult = paymentGateway.processPayment(
                request.getPaymentMethod(),
                fareResult.getTotalFare(),
                request.getCardToken()
        );

        PaymentStatus status = gatewayResult.isSuccess() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED;
        Long driverId = (request.getDriverId() != null) ? request.getDriverId() : ride.getDriverId();

        // 5. Persist payment audit record
        Payment payment = Payment.builder()
                .rideId(request.getRideId())
                .passengerId(request.getPassengerId())
                .driverId(driverId)
                .amount(fareResult.getTotalFare())
                .baseFare(fareResult.getBaseFare())
                .distanceFare(fareResult.getDistanceFare())
                .timeFare(fareResult.getTimeFare())
                .vehicleMultiplier(fareResult.getVehicleMultiplier())
                .distanceKm(fareResult.getDistanceKm())
                .durationMinutes(fareResult.getDurationMinutes())
                .paymentMethod(request.getPaymentMethod())
                .status(status)
                .transactionId(gatewayResult.getTransactionId())
                .cardLast4(gatewayResult.getCardLast4())
                .failureReason(gatewayResult.getFailureReason())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // 6. If payment gateway declined, throw PaymentFailedException so client gets 400 Bad Request
        if (!gatewayResult.isSuccess()) {
            throw new PaymentFailedException("Payment processing failed: " + gatewayResult.getFailureReason());
        }

        return mapToPaymentResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        return mapToPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByRideId(Long rideId) {
        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for ride id: " + rideId));
        return mapToPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptByPaymentId(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        BigDecimal subtotal = BigDecimal.ZERO;
        if (payment.getBaseFare() != null && payment.getDistanceFare() != null && payment.getTimeFare() != null) {
            subtotal = payment.getBaseFare().add(payment.getDistanceFare()).add(payment.getTimeFare());
        } else {
            subtotal = payment.getAmount();
        }

        String maskedCard = payment.getCardLast4() != null
                ? "**** **** **** " + payment.getCardLast4()
                : null;

        return ReceiptResponse.builder()
                .receiptNumber("REC-" + payment.getId())
                .transactionId(payment.getTransactionId())
                .paymentId(payment.getId())
                .rideId(payment.getRideId())
                .passengerId(payment.getPassengerId())
                .driverId(payment.getDriverId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getStatus())
                .maskedCardNumber(maskedCard)
                .distanceKm(payment.getDistanceKm())
                .durationMinutes(payment.getDurationMinutes())
                .baseFare(payment.getBaseFare())
                .distanceFare(payment.getDistanceFare())
                .timeFare(payment.getTimeFare())
                .vehicleMultiplier(payment.getVehicleMultiplier())
                .subtotal(subtotal)
                .totalAmount(payment.getAmount())
                .currency(fareProperties.getCurrency())
                .issuedAt(payment.getCreatedAt())
                .build();
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .rideId(payment.getRideId())
                .passengerId(payment.getPassengerId())
                .driverId(payment.getDriverId())
                .amount(payment.getAmount())
                .baseFare(payment.getBaseFare())
                .distanceFare(payment.getDistanceFare())
                .timeFare(payment.getTimeFare())
                .vehicleMultiplier(payment.getVehicleMultiplier())
                .distanceKm(payment.getDistanceKm())
                .durationMinutes(payment.getDurationMinutes())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .cardLast4(payment.getCardLast4())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}

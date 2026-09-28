package lk.sliit.it3130.fare_payment_service.service;

import lk.sliit.it3130.fare_payment_service.client.RideClient;
import lk.sliit.it3130.fare_payment_service.config.FareProperties;
import lk.sliit.it3130.fare_payment_service.dto.*;
import lk.sliit.it3130.fare_payment_service.exception.DuplicatePaymentException;
import lk.sliit.it3130.fare_payment_service.exception.PaymentFailedException;
import lk.sliit.it3130.fare_payment_service.exception.ResourceNotFoundException;
import lk.sliit.it3130.fare_payment_service.exception.RideNotCompletedException;
import lk.sliit.it3130.fare_payment_service.model.Payment;
import lk.sliit.it3130.fare_payment_service.model.PaymentMethod;
import lk.sliit.it3130.fare_payment_service.model.PaymentStatus;
import lk.sliit.it3130.fare_payment_service.model.VehicleType;
import lk.sliit.it3130.fare_payment_service.repository.PaymentRepository;
import lk.sliit.it3130.fare_payment_service.service.gateway.PaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RideClient rideClient;

    @Mock
    private FareService fareService;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private FareProperties fareProperties;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentRequest validPaymentRequest;
    private RideResponse completedRide;
    private FareCalculationResult fareCalculationResult;

    @BeforeEach
    void setUp() {
        validPaymentRequest = PaymentRequest.builder()
                .rideId(101L)
                .passengerId(201L)
                .driverId(301L)
                .paymentMethod(PaymentMethod.CARD)
                .cardToken("tok_visa_4242")
                .build();

        completedRide = RideResponse.builder()
                .id(101L)
                .passengerId(201L)
                .driverId(301L)
                .status("COMPLETED")
                .vehicleType(VehicleType.CAR)
                .distanceKm(10.0)
                .durationMinutes(20.0)
                .build();

        fareCalculationResult = FareCalculationResult.builder()
                .distanceKm(10.0)
                .durationMinutes(20.0)
                .vehicleType(VehicleType.CAR)
                .baseFare(BigDecimal.valueOf(150.00))
                .distanceFare(BigDecimal.valueOf(800.00))
                .timeFare(BigDecimal.valueOf(100.00))
                .vehicleMultiplier(1.2)
                .subtotal(BigDecimal.valueOf(1050.00))
                .totalFare(BigDecimal.valueOf(1260.00))
                .minimumFare(BigDecimal.valueOf(200.00))
                .currency("LKR")
                .build();
    }

    @Test
    @DisplayName("Should successfully process payment when ride is COMPLETED and gateway succeeds")
    void testProcessPaymentSuccess() {
        when(rideClient.getRideById(101L)).thenReturn(completedRide);
        when(paymentRepository.existsByRideIdAndStatus(101L, PaymentStatus.COMPLETED)).thenReturn(false);
        when(fareService.calculateFare(10.0, 20.0, VehicleType.CAR)).thenReturn(fareCalculationResult);

        GatewayTransactionResult gatewayResult = GatewayTransactionResult.builder()
                .success(true)
                .transactionId("TXN-CARD-999888")
                .cardLast4("4242")
                .message("Success")
                .build();
        when(paymentGateway.processPayment(PaymentMethod.CARD, BigDecimal.valueOf(1260.00), "tok_visa_4242"))
                .thenReturn(gatewayResult);

        Payment savedPayment = Payment.builder()
                .id(1L)
                .rideId(101L)
                .passengerId(201L)
                .driverId(301L)
                .amount(BigDecimal.valueOf(1260.00))
                .baseFare(BigDecimal.valueOf(150.00))
                .distanceFare(BigDecimal.valueOf(800.00))
                .timeFare(BigDecimal.valueOf(100.00))
                .vehicleMultiplier(1.2)
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.COMPLETED)
                .transactionId("TXN-CARD-999888")
                .cardLast4("4242")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentResponse response = paymentService.processPayment(validPaymentRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(101L, response.getRideId());
        assertEquals(BigDecimal.valueOf(1260.00), response.getAmount());
        assertEquals(PaymentStatus.COMPLETED, response.getStatus());
        assertEquals("TXN-CARD-999888", response.getTransactionId());
        assertEquals("4242", response.getCardLast4());

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw RideNotCompletedException when ride is IN_PROGRESS")
    void testProcessPaymentRideNotInProgress() {
        completedRide.setStatus("IN_PROGRESS");
        when(rideClient.getRideById(101L)).thenReturn(completedRide);

        RideNotCompletedException exception = assertThrows(RideNotCompletedException.class, () ->
                paymentService.processPayment(validPaymentRequest));

        assertTrue(exception.getMessage().contains("IN_PROGRESS"));
        verify(paymentRepository, never()).save(any());
        verify(paymentGateway, never()).processPayment(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw DuplicatePaymentException when ride was already paid")
    void testProcessPaymentDuplicatePaymentRejection() {
        when(rideClient.getRideById(101L)).thenReturn(completedRide);
        when(paymentRepository.existsByRideIdAndStatus(101L, PaymentStatus.COMPLETED)).thenReturn(true);

        DuplicatePaymentException exception = assertThrows(DuplicatePaymentException.class, () ->
                paymentService.processPayment(validPaymentRequest));

        assertTrue(exception.getMessage().contains("already been recorded"));
        verify(paymentGateway, never()).processPayment(any(), any(), any());
    }

    @Test
    @DisplayName("Should fail payment and throw PaymentFailedException when card token ends in 0000")
    void testProcessPaymentCardToken0000Declined() {
        validPaymentRequest.setCardToken("tok_declined_0000");

        when(rideClient.getRideById(101L)).thenReturn(completedRide);
        when(paymentRepository.existsByRideIdAndStatus(101L, PaymentStatus.COMPLETED)).thenReturn(false);
        when(fareService.calculateFare(10.0, 20.0, VehicleType.CAR)).thenReturn(fareCalculationResult);

        GatewayTransactionResult declinedResult = GatewayTransactionResult.builder()
                .success(false)
                .cardLast4("0000")
                .failureReason("Card payment declined for test token ending in 0000")
                .build();
        when(paymentGateway.processPayment(PaymentMethod.CARD, BigDecimal.valueOf(1260.00), "tok_declined_0000"))
                .thenReturn(declinedResult);

        Payment failedPayment = Payment.builder()
                .id(2L)
                .rideId(101L)
                .passengerId(201L)
                .amount(BigDecimal.valueOf(1260.00))
                .status(PaymentStatus.FAILED)
                .cardLast4("0000")
                .failureReason("Card payment declined for test token ending in 0000")
                .build();
        when(paymentRepository.save(any(Payment.class))).thenReturn(failedPayment);

        PaymentFailedException exception = assertThrows(PaymentFailedException.class, () ->
                paymentService.processPayment(validPaymentRequest));

        assertTrue(exception.getMessage().contains("declined"));
        verify(paymentRepository).save(argThat(payment -> payment.getStatus() == PaymentStatus.FAILED));
    }

    @Test
    @DisplayName("Should retrieve payment by ID or throw ResourceNotFoundException")
    void testGetPaymentById() {
        Payment payment = Payment.builder()
                .id(1L)
                .rideId(101L)
                .passengerId(201L)
                .amount(BigDecimal.valueOf(1260.00))
                .status(PaymentStatus.COMPLETED)
                .build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentById(1L);
        assertNotNull(response);
        assertEquals(1L, response.getId());

        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentById(999L));
    }

    @Test
    @DisplayName("Should retrieve payment by Ride ID")
    void testGetPaymentByRideId() {
        Payment payment = Payment.builder()
                .id(1L)
                .rideId(101L)
                .passengerId(201L)
                .amount(BigDecimal.valueOf(1260.00))
                .status(PaymentStatus.COMPLETED)
                .build();
        when(paymentRepository.findByRideId(101L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentByRideId(101L);
        assertNotNull(response);
        assertEquals(101L, response.getRideId());
    }

    @Test
    @DisplayName("Should generate formatted receipt with masked card number")
    void testGetReceiptByPaymentId() {
        Payment payment = Payment.builder()
                .id(1L)
                .rideId(101L)
                .passengerId(201L)
                .driverId(301L)
                .amount(BigDecimal.valueOf(1260.00))
                .baseFare(BigDecimal.valueOf(150.00))
                .distanceFare(BigDecimal.valueOf(800.00))
                .timeFare(BigDecimal.valueOf(100.00))
                .vehicleMultiplier(1.2)
                .distanceKm(10.0)
                .durationMinutes(20.0)
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.COMPLETED)
                .transactionId("TXN-CARD-123456")
                .cardLast4("4242")
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(fareProperties.getCurrency()).thenReturn("LKR");

        ReceiptResponse receipt = paymentService.getReceiptByPaymentId(1L);

        assertNotNull(receipt);
        assertEquals("REC-1", receipt.getReceiptNumber());
        assertEquals("TXN-CARD-123456", receipt.getTransactionId());
        assertEquals("**** **** **** 4242", receipt.getMaskedCardNumber());
        assertEquals(BigDecimal.valueOf(1260.00), receipt.getTotalAmount());
        assertEquals("LKR", receipt.getCurrency());
    }
}

package lk.sliit.it3130.fare_payment_service.service;

import lk.sliit.it3130.fare_payment_service.dto.PaymentRequest;
import lk.sliit.it3130.fare_payment_service.dto.PaymentResponse;
import lk.sliit.it3130.fare_payment_service.dto.ReceiptResponse;

public interface PaymentService {

    /**
     * Processes a payment for a completed ride.
     * Validates ride status with the Ride Management Service, prevents duplicate payments,
     * calculates the final fare, calls the payment gateway, and persists transaction state.
     *
     * @param request PaymentRequest containing rideId, passengerId, paymentMethod, and cardToken
     * @return PaymentResponse detailing the processed transaction
     */
    PaymentResponse processPayment(PaymentRequest request);

    /**
     * Retrieves a payment record by its unique database ID.
     *
     * @param paymentId Unique payment identifier
     * @return PaymentResponse
     */
    PaymentResponse getPaymentById(Long paymentId);

    /**
     * Retrieves a payment record associated with a given ride ID.
     *
     * @param rideId Identifier of the ride
     * @return PaymentResponse
     */
    PaymentResponse getPaymentByRideId(Long rideId);

    /**
     * Generates a formatted itemized receipt for a completed payment.
     *
     * @param paymentId Unique payment identifier
     * @return ReceiptResponse containing itemized fare breakdown and transaction details
     */
    ReceiptResponse getReceiptByPaymentId(Long paymentId);
}

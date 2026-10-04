package lk.sliit.it3130.fare_payment_service.dto;

import jakarta.validation.constraints.NotNull;
import lk.sliit.it3130.fare_payment_service.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Ride ID is required")
    private Long rideId;

    @NotNull(message = "Passenger ID is required")
    private Long passengerId;

    private Long driverId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    /**
     * Card token for card transactions.
     * Note: Simulated rule triggers failure if cardToken ends in '0000'.
     */
    private String cardToken;
}

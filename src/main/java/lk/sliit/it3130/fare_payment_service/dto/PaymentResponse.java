package lk.sliit.it3130.fare_payment_service.dto;

import lk.sliit.it3130.fare_payment_service.model.PaymentMethod;
import lk.sliit.it3130.fare_payment_service.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long rideId;
    private Long passengerId;
    private Long driverId;
    private BigDecimal amount;
    private BigDecimal baseFare;
    private BigDecimal distanceFare;
    private BigDecimal timeFare;
    private Double vehicleMultiplier;
    private Double distanceKm;
    private Double durationMinutes;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionId;
    private String cardLast4;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

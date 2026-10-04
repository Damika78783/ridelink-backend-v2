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
public class ReceiptResponse {

    private String receiptNumber;
    private String transactionId;
    private Long paymentId;
    private Long rideId;
    private Long passengerId;
    private Long driverId;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String maskedCardNumber;
    private Double distanceKm;
    private Double durationMinutes;
    private BigDecimal baseFare;
    private BigDecimal distanceFare;
    private BigDecimal timeFare;
    private Double vehicleMultiplier;
    private BigDecimal subtotal;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime issuedAt;
}

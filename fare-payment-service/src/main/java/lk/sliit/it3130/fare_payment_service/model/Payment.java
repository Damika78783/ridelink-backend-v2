package lk.sliit.it3130.fare_payment_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_ride_id", columnList = "rideId"),
        @Index(name = "idx_payments_passenger_id", columnList = "passengerId"),
        @Index(name = "idx_payments_transaction_id", columnList = "transactionId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long rideId;

    @Column(nullable = false)
    private Long passengerId;

    @Column
    private Long driverId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(precision = 10, scale = 2)
    private BigDecimal baseFare;

    @Column(precision = 10, scale = 2)
    private BigDecimal distanceFare;

    @Column(precision = 10, scale = 2)
    private BigDecimal timeFare;

    @Column
    private Double vehicleMultiplier;

    @Column
    private Double distanceKm;

    @Column
    private Double durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(unique = true, length = 100)
    private String transactionId;

    @Column(length = 4)
    private String cardLast4;

    @Column(length = 255)
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PaymentStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

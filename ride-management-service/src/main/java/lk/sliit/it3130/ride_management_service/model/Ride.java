package lk.sliit.it3130.ride_management_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rides")
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String passengerEmail;

    // nullable — driver assigned later
    private String driverEmail;

    @Column(nullable = false)
    private String pickupLocation;

    @Column(nullable = false)
    private String destinationLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status;

    private Double fareEstimateId; // placeholder reference — real fare comes from Fare & Payment Service later

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.requestedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = RideStatus.REQUESTED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    public String getDriverEmail() {
        return driverEmail;
    }

    public void setDriverEmail(String driverEmail) {
        this.driverEmail = driverEmail;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(String destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public Double getFareEstimateId() {
        return fareEstimateId;
    }

    public void setFareEstimateId(Double fareEstimateId) {
        this.fareEstimateId = fareEstimateId;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
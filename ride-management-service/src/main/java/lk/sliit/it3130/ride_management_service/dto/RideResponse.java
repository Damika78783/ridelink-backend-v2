package lk.sliit.it3130.ride_management_service.dto;

import lk.sliit.it3130.ride_management_service.model.RideStatus;
import java.time.LocalDateTime;

public class RideResponse {

    private Long id;
    private String passengerEmail;
    private String driverEmail;
    private String pickupLocation;
    private String destinationLocation;
    private RideStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime updatedAt;

    public RideResponse() {
    }

    public RideResponse(Long id, String passengerEmail, String driverEmail,
                         String pickupLocation, String destinationLocation,
                         RideStatus status, LocalDateTime requestedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.passengerEmail = passengerEmail;
        this.driverEmail = driverEmail;
        this.pickupLocation = pickupLocation;
        this.destinationLocation = destinationLocation;
        this.status = status;
        this.requestedAt = requestedAt;
        this.updatedAt = updatedAt;
    }

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

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
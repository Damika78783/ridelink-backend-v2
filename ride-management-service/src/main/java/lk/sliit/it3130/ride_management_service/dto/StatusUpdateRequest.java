package lk.sliit.it3130.ride_management_service.dto;

import lk.sliit.it3130.ride_management_service.model.RideStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private RideStatus status;

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }
}
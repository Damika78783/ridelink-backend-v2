package com.ridelink.drivervehicle.dto.request;

import com.ridelink.drivervehicle.model.AvailabilityStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating driver availability status")
public class UpdateAvailabilityRequest {

    @NotNull(message = "Availability status is mandatory (AVAILABLE, UNAVAILABLE, ON_TRIP, OFFLINE)")
    @Schema(description = "Driver operational availability status", example = "AVAILABLE")
    private AvailabilityStatus availabilityStatus;
}

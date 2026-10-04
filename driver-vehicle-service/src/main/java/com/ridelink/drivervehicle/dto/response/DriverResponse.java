package com.ridelink.drivervehicle.dto.response;

import com.ridelink.drivervehicle.model.AvailabilityStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload representing driver operational profile")
public class DriverResponse {

    @Schema(description = "Driver profile unique ID", example = "66f42a1b9c3e4a2d8f1e5a7b")
    private String id;

    @Schema(description = "Account ID from Account Service", example = "acc-60d5ecb8b39f")
    private String accountId;

    @Schema(description = "Driver official license number", example = "B1234567")
    private String licenseNumber;

    @Schema(description = "Operational availability status", example = "AVAILABLE")
    private AvailabilityStatus availabilityStatus;

    @Schema(description = "Designated operational service area", example = "Colombo")
    private String serviceArea;

    @Schema(description = "Current simulated geographical location")
    private LocationResponse currentLocation;

    @Schema(description = "Assigned vehicle details (null if no vehicle registered yet)")
    private VehicleResponse vehicle;

    @Schema(description = "Profile creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last profile update timestamp")
    private LocalDateTime updatedAt;
}

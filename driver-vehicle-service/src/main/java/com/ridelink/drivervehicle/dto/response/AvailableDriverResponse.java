package com.ridelink.drivervehicle.dto.response;

import com.ridelink.drivervehicle.model.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload representing an eligible available driver for ride dispatching")
public class AvailableDriverResponse {

    @Schema(description = "Driver profile ID", example = "66f42a1b9c3e4a2d8f1e5a7b")
    private String driverId;

    @Schema(description = "Account ID from Account Service", example = "acc-60d5ecb8b39f")
    private String accountId;

    @Schema(description = "Driving license number", example = "B1234567")
    private String licenseNumber;

    @Schema(description = "Operational service area", example = "Colombo")
    private String serviceArea;

    @Schema(description = "Driver simulated location")
    private LocationResponse currentLocation;

    @Schema(description = "Registered vehicle ID", example = "66f42b3c9c3e4a2d8f1e5a7c")
    private String vehicleId;

    @Schema(description = "Registered vehicle license plate number", example = "WP-CAB-1234")
    private String vehicleNumber;

    @Schema(description = "Registered vehicle category", example = "CAR")
    private VehicleType vehicleType;

    @Schema(description = "Registered vehicle make and model", example = "Toyota Prius 2020")
    private String vehicleModel;

    @Schema(description = "Simulated distance in km from pickup coordinate (if requested)", example = "2.35")
    private Double distanceKm;
}

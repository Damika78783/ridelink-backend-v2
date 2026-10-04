package com.ridelink.drivervehicle.dto.request;

import com.ridelink.drivervehicle.model.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating vehicle details")
public class UpdateVehicleRequest {

    @Pattern(regexp = "^[A-Za-z0-9 -]{4,15}$", message = "Vehicle registration number must be 4 to 15 alphanumeric characters")
    @Schema(description = "Updated vehicle registration number", example = "WP-CAA-5678")
    private String vehicleNumber;

    @Schema(description = "Updated vehicle category", example = "SUV")
    private VehicleType vehicleType;

    @Schema(description = "Updated vehicle make and model description", example = "Honda Vezel 2021")
    private String model;
}

package com.ridelink.drivervehicle.dto.request;

import com.ridelink.drivervehicle.model.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for registering a vehicle to a driver")
public class CreateVehicleRequest {

    @NotBlank(message = "Driver ID is mandatory")
    @Schema(description = "ID of the driver who owns/operates this vehicle", example = "66f42a1b9c3e4a2d8f1e5a7b")
    private String driverId;

    @NotBlank(message = "Vehicle number is mandatory")
    @Pattern(regexp = "^[A-Za-z0-9 -]{4,15}$", message = "Vehicle registration number must be 4 to 15 alphanumeric characters")
    @Schema(description = "Official vehicle license plate number", example = "WP-CAB-1234")
    private String vehicleNumber;

    @NotNull(message = "Vehicle type is mandatory (CAR, VAN, MOTORBIKE, TUK_TUK, SUV)")
    @Schema(description = "Category of the vehicle", example = "CAR")
    private VehicleType vehicleType;

    @NotBlank(message = "Vehicle model is mandatory")
    @Schema(description = "Vehicle make and model description", example = "Toyota Prius 2020")
    private String model;
}

package com.ridelink.drivervehicle.dto.request;

import com.ridelink.drivervehicle.model.AvailabilityStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a driver operational profile")
public class CreateDriverRequest {

    @NotBlank(message = "Account ID is mandatory")
    @Schema(description = "Account ID issued by Account Service", example = "acc-60d5ecb8b39f")
    private String accountId;

    @NotBlank(message = "Driving license number is mandatory")
    @Pattern(regexp = "^[A-Za-z0-9-]{5,20}$", message = "License number must be between 5 and 20 alphanumeric characters")
    @Schema(description = "Driver official license number", example = "B1234567")
    private String licenseNumber;

    @NotBlank(message = "Service area is mandatory")
    @Schema(description = "Designated operational service area (e.g. city or region)", example = "Colombo")
    private String serviceArea;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
    @Schema(description = "Initial simulated latitude", example = "6.9271")
    private Double initialLatitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
    @Schema(description = "Initial simulated longitude", example = "79.8612")
    private Double initialLongitude;

    @Schema(description = "Initial simulated street address or landmark", example = "Colombo Fort")
    private String initialAddress;

    @Schema(description = "Initial availability status", example = "OFFLINE")
    private AvailabilityStatus availabilityStatus;
}

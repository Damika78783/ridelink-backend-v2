package com.ridelink.drivervehicle.dto.request;

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
@Schema(description = "Request payload for updating general driver operational profile")
public class UpdateDriverRequest {

    @Pattern(regexp = "^[A-Za-z0-9-]{5,20}$", message = "License number must be between 5 and 20 alphanumeric characters")
    @Schema(description = "Updated driving license number", example = "B9876543")
    private String licenseNumber;

    @Schema(description = "Updated operational service area", example = "Kandy")
    private String serviceArea;
}

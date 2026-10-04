package com.ridelink.drivervehicle.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating driver service area")
public class UpdateServiceAreaRequest {

    @NotBlank(message = "Service area is mandatory")
    @Schema(description = "Designated operational service area (city or region)", example = "Colombo")
    private String serviceArea;
}

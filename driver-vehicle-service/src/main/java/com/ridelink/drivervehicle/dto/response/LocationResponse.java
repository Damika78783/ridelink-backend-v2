package com.ridelink.drivervehicle.dto.response;

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
@Schema(description = "Response payload representing simulated location details")
public class LocationResponse {

    @Schema(description = "Latitude coordinate", example = "6.9271")
    private Double latitude;

    @Schema(description = "Longitude coordinate", example = "79.8612")
    private Double longitude;

    @Schema(description = "Street address or landmark label", example = "Colombo Fort")
    private String address;

    @Schema(description = "Timestamp when location was recorded")
    private LocalDateTime updatedAt;
}

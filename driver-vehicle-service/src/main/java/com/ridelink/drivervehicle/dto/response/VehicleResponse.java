package com.ridelink.drivervehicle.dto.response;

import com.ridelink.drivervehicle.model.VehicleType;
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
@Schema(description = "Response payload representing vehicle details")
public class VehicleResponse {

    @Schema(description = "Vehicle unique ID", example = "66f42b3c9c3e4a2d8f1e5a7c")
    private String id;

    @Schema(description = "ID of the associated driver", example = "66f42a1b9c3e4a2d8f1e5a7b")
    private String driverId;

    @Schema(description = "Vehicle license plate number", example = "WP-CAB-1234")
    private String vehicleNumber;

    @Schema(description = "Vehicle category", example = "CAR")
    private VehicleType vehicleType;

    @Schema(description = "Vehicle model details", example = "Toyota Prius 2020")
    private String model;

    @Schema(description = "Registration timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}

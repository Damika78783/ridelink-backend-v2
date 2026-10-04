package com.ridelink.drivervehicle.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Embedded document representing simulated geographical coordinates of a driver.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    private Double latitude;
    private Double longitude;
    private String address;

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}

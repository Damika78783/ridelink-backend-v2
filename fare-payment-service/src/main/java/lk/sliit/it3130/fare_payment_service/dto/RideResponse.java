package lk.sliit.it3130.fare_payment_service.dto;

import lk.sliit.it3130.fare_payment_service.model.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideResponse {

    private Long id;
    private Long passengerId;
    private Long driverId;
    private String status;
    private VehicleType vehicleType;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double dropoffLatitude;
    private Double dropoffLongitude;
    private Double distanceKm;
    private Double durationMinutes;
    private BigDecimal fare;
}

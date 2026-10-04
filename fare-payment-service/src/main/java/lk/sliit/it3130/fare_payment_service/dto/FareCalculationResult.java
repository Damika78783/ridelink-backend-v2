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
public class FareCalculationResult {

    private double distanceKm;
    private double durationMinutes;
    private VehicleType vehicleType;
    private BigDecimal baseFare;
    private BigDecimal distanceFare;
    private BigDecimal timeFare;
    private double vehicleMultiplier;
    private BigDecimal subtotal;
    private BigDecimal totalFare;
    private BigDecimal minimumFare;
    private boolean minimumFareApplied;
    private String currency;
}

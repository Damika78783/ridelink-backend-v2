package lk.sliit.it3130.fare_payment_service.service.fare;

import lk.sliit.it3130.fare_payment_service.dto.FareCalculationResult;
import lk.sliit.it3130.fare_payment_service.model.VehicleType;

public interface FareCalculationStrategy {

    /**
     * Calculates the estimated or final fare based on distance, duration, and vehicle type.
     * Formula: max(minimumFare, (baseFare + distanceKm * perKmRate + durationMin * perMinRate) * vehicleMultiplier)
     *
     * @param distanceKm   Total distance in kilometers
     * @param durationMin  Total estimated or actual duration in minutes
     * @param vehicleType  Type of vehicle (CAR, VAN, TUKTUK, BIKE)
     * @return FareCalculationResult containing detailed fare breakdown
     */
    FareCalculationResult calculateFare(double distanceKm, double durationMin, VehicleType vehicleType);
}

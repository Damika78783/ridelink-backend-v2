package lk.sliit.it3130.fare_payment_service.service;

import lk.sliit.it3130.fare_payment_service.dto.FareCalculationResult;
import lk.sliit.it3130.fare_payment_service.dto.FareEstimateRequest;
import lk.sliit.it3130.fare_payment_service.dto.FareEstimateResponse;
import lk.sliit.it3130.fare_payment_service.model.VehicleType;

public interface FareService {

    /**
     * Estimates the fare for a trip based on pickup/destination GPS coordinates and vehicle type.
     *
     * @param request FareEstimateRequest containing coordinates and vehicle type
     * @return FareEstimateResponse with estimated fare and cost breakdown
     */
    FareEstimateResponse estimateFare(FareEstimateRequest request);

    /**
     * Calculates the exact fare for completed ride metrics.
     *
     * @param distanceKm   Total distance in kilometers
     * @param durationMin  Total duration in minutes
     * @param vehicleType  Type of vehicle
     * @return FareCalculationResult breakdown
     */
    FareCalculationResult calculateFare(double distanceKm, double durationMin, VehicleType vehicleType);

    /**
     * Calculates GPS distance in kilometers using the Haversine formula.
     */
    double calculateDistance(double startLat, double startLon, double endLat, double endLon);

    /**
     * Estimates trip duration in minutes from distance using standard city traffic assumptions.
     */
    double estimateDurationMinutes(double distanceKm);
}

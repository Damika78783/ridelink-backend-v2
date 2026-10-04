package lk.sliit.it3130.fare_payment_service.service;

import lk.sliit.it3130.fare_payment_service.dto.FareCalculationResult;
import lk.sliit.it3130.fare_payment_service.dto.FareEstimateRequest;
import lk.sliit.it3130.fare_payment_service.dto.FareEstimateResponse;
import lk.sliit.it3130.fare_payment_service.model.VehicleType;
import lk.sliit.it3130.fare_payment_service.service.fare.FareCalculationStrategy;
import lk.sliit.it3130.fare_payment_service.service.fare.HaversineDistanceCalculator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FareServiceImpl implements FareService {

    private static final double AVERAGE_CITY_SPEED_KMH = 25.0;

    private final FareCalculationStrategy fareCalculationStrategy;
    private final HaversineDistanceCalculator haversineDistanceCalculator;

    public FareServiceImpl(FareCalculationStrategy fareCalculationStrategy,
                           HaversineDistanceCalculator haversineDistanceCalculator) {
        this.fareCalculationStrategy = fareCalculationStrategy;
        this.haversineDistanceCalculator = haversineDistanceCalculator;
    }

    @Override
    public FareEstimateResponse estimateFare(FareEstimateRequest request) {
        double distanceKm = calculateDistance(
                request.getPickupLatitude(),
                request.getPickupLongitude(),
                request.getDestinationLatitude(),
                request.getDestinationLongitude()
        );

        double durationMin = (request.getEstimatedDurationMin() != null && request.getEstimatedDurationMin() > 0)
                ? request.getEstimatedDurationMin()
                : estimateDurationMinutes(distanceKm);

        FareCalculationResult result = fareCalculationStrategy.calculateFare(
                distanceKm,
                durationMin,
                request.getVehicleType()
        );

        return FareEstimateResponse.builder()
                .distanceKm(result.getDistanceKm())
                .estimatedDurationMin(result.getDurationMinutes())
                .vehicleType(result.getVehicleType())
                .baseFare(result.getBaseFare())
                .distanceFare(result.getDistanceFare())
                .timeFare(result.getTimeFare())
                .vehicleMultiplier(result.getVehicleMultiplier())
                .estimatedFare(result.getTotalFare())
                .minimumFare(result.getMinimumFare())
                .currency(result.getCurrency())
                .build();
    }

    @Override
    public FareCalculationResult calculateFare(double distanceKm, double durationMin, VehicleType vehicleType) {
        return fareCalculationStrategy.calculateFare(distanceKm, durationMin, vehicleType);
    }

    @Override
    public double calculateDistance(double startLat, double startLon, double endLat, double endLon) {
        return haversineDistanceCalculator.calculateDistanceKm(startLat, startLon, endLat, endLon);
    }

    @Override
    public double estimateDurationMinutes(double distanceKm) {
        if (distanceKm <= 0) {
            return 0.0;
        }
        double rawMinutes = (distanceKm / AVERAGE_CITY_SPEED_KMH) * 60.0;
        return BigDecimal.valueOf(rawMinutes)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}

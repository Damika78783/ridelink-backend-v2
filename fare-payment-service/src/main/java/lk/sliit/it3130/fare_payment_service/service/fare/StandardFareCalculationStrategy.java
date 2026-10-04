package lk.sliit.it3130.fare_payment_service.service.fare;

import lk.sliit.it3130.fare_payment_service.config.FareProperties;
import lk.sliit.it3130.fare_payment_service.dto.FareCalculationResult;
import lk.sliit.it3130.fare_payment_service.exception.InvalidFareException;
import lk.sliit.it3130.fare_payment_service.model.VehicleType;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Primary
public class StandardFareCalculationStrategy implements FareCalculationStrategy {

    private final FareProperties fareProperties;

    public StandardFareCalculationStrategy(FareProperties fareProperties) {
        this.fareProperties = fareProperties;
    }

    @Override
    public FareCalculationResult calculateFare(double distanceKm, double durationMin, VehicleType vehicleType) {
        if (distanceKm < 0 || durationMin < 0) {
            throw new InvalidFareException("Distance and duration cannot be negative values");
        }
        if (vehicleType == null) {
            throw new InvalidFareException("Vehicle type cannot be null");
        }

        BigDecimal baseFare = fareProperties.getBaseFare().setScale(2, RoundingMode.HALF_UP);

        BigDecimal distanceFare = BigDecimal.valueOf(distanceKm)
                .multiply(fareProperties.getPerKmRate())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal timeFare = BigDecimal.valueOf(durationMin)
                .multiply(fareProperties.getPerMinRate())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal subtotal = baseFare.add(distanceFare).add(timeFare)
                .setScale(2, RoundingMode.HALF_UP);

        double multiplier = fareProperties.getMultiplier(vehicleType);
        BigDecimal multiplierBigDecimal = BigDecimal.valueOf(multiplier);

        BigDecimal calculatedFare = subtotal.multiply(multiplierBigDecimal)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal minimumFare = fareProperties.getMinimumFare().setScale(2, RoundingMode.HALF_UP);

        boolean minimumFareApplied = calculatedFare.compareTo(minimumFare) < 0;
        BigDecimal finalFare = minimumFareApplied ? minimumFare : calculatedFare;

        return FareCalculationResult.builder()
                .distanceKm(distanceKm)
                .durationMinutes(durationMin)
                .vehicleType(vehicleType)
                .baseFare(baseFare)
                .distanceFare(distanceFare)
                .timeFare(timeFare)
                .vehicleMultiplier(multiplier)
                .subtotal(subtotal)
                .totalFare(finalFare)
                .minimumFare(minimumFare)
                .minimumFareApplied(minimumFareApplied)
                .currency(fareProperties.getCurrency())
                .build();
    }
}

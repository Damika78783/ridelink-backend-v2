package lk.sliit.it3130.fare_payment_service.service.fare;

import lk.sliit.it3130.fare_payment_service.config.FareProperties;
import lk.sliit.it3130.fare_payment_service.dto.FareCalculationResult;
import lk.sliit.it3130.fare_payment_service.exception.InvalidFareException;
import lk.sliit.it3130.fare_payment_service.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FareCalculationStrategyTest {

    private StandardFareCalculationStrategy strategy;
    private FareProperties fareProperties;

    @BeforeEach
    void setUp() {
        fareProperties = new FareProperties();
        fareProperties.setBaseFare(BigDecimal.valueOf(150.00));
        fareProperties.setPerKmRate(BigDecimal.valueOf(80.00));
        fareProperties.setPerMinRate(BigDecimal.valueOf(5.00));
        fareProperties.setMinimumFare(BigDecimal.valueOf(200.00));
        fareProperties.setCurrency("LKR");
        fareProperties.setMultipliers(Map.of(
                "car", 1.2,
                "van", 1.5,
                "tuktuk", 1.0,
                "bike", 0.8
        ));

        strategy = new StandardFareCalculationStrategy(fareProperties);
    }

    @Test
    @DisplayName("Should calculate correct fare for standard CAR trip")
    void testCalculateFareForCar() {
        // distance: 10 km, duration: 20 min
        // base: 150, distanceFare: 10 * 80 = 800, timeFare: 20 * 5 = 100
        // subtotal = 150 + 800 + 100 = 1050
        // CAR multiplier = 1.2 -> 1050 * 1.2 = 1260.00
        FareCalculationResult result = strategy.calculateFare(10.0, 20.0, VehicleType.CAR);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(150.00).setScale(2), result.getBaseFare());
        assertEquals(BigDecimal.valueOf(800.00).setScale(2), result.getDistanceFare());
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), result.getTimeFare());
        assertEquals(BigDecimal.valueOf(1050.00).setScale(2), result.getSubtotal());
        assertEquals(1.2, result.getVehicleMultiplier());
        assertEquals(BigDecimal.valueOf(1260.00).setScale(2), result.getTotalFare());
        assertFalse(result.isMinimumFareApplied());
    }

    @Test
    @DisplayName("Should apply minimum fare when calculated amount is below minimum threshold")
    void testMinimumFareApplication() {
        // distance: 0.1 km, duration: 1 min, BIKE (0.8)
        // subtotal = 150 + (0.1 * 80 = 8) + (1 * 5 = 5) = 163
        // bike: 163 * 0.8 = 130.40 -> below min fare 200.00
        FareCalculationResult result = strategy.calculateFare(0.1, 1.0, VehicleType.BIKE);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(200.00).setScale(2), result.getTotalFare());
        assertTrue(result.isMinimumFareApplied());
    }

    @Test
    @DisplayName("Should calculate correct multiplier for VAN and TUKTUK")
    void testVehicleMultipliers() {
        // subtotal = 150 + (5 * 80 = 400) + (10 * 5 = 50) = 600.00
        // VAN (1.5) -> 600 * 1.5 = 900.00
        FareCalculationResult vanResult = strategy.calculateFare(5.0, 10.0, VehicleType.VAN);
        assertEquals(BigDecimal.valueOf(900.00).setScale(2), vanResult.getTotalFare());

        // TUKTUK (1.0) -> 600 * 1.0 = 600.00
        FareCalculationResult tuktukResult = strategy.calculateFare(5.0, 10.0, VehicleType.TUKTUK);
        assertEquals(BigDecimal.valueOf(600.00).setScale(2), tuktukResult.getTotalFare());
    }

    @Test
    @DisplayName("Should throw InvalidFareException for negative distance or duration")
    void testNegativeInputsThrowException() {
        assertThrows(InvalidFareException.class, () ->
                strategy.calculateFare(-5.0, 10.0, VehicleType.CAR));

        assertThrows(InvalidFareException.class, () ->
                strategy.calculateFare(5.0, -10.0, VehicleType.CAR));

        assertThrows(InvalidFareException.class, () ->
                strategy.calculateFare(5.0, 10.0, null));
    }
}

package lk.sliit.it3130.fare_payment_service.service.fare;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HaversineDistanceCalculatorTest {

    private HaversineDistanceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new HaversineDistanceCalculator();
    }

    @Test
    @DisplayName("Should return 0 km when start and end coordinates are identical")
    void testSameCoordinatesReturnZero() {
        double distance = calculator.calculateDistanceKm(6.9271, 79.8612, 6.9271, 79.8612);
        assertEquals(0.0, distance, 0.001);
    }

    @Test
    @DisplayName("Should calculate accurate distance between Colombo and Kandy (~95-105 km)")
    void testDistanceBetweenColomboAndKandy() {
        // Colombo Fort: 6.9344, 79.8428
        // Kandy Clock Tower: 7.2936, 80.6350
        double distance = calculator.calculateDistanceKm(6.9344, 79.8428, 7.2936, 80.6350);

        assertTrue(distance > 90.0 && distance < 110.0,
                "Expected distance between Colombo and Kandy to be ~95-105 km, but got: " + distance);
    }

    @Test
    @DisplayName("Should calculate short distance within Colombo (~3-5 km)")
    void testShortUrbanDistance() {
        // Colombo Fort to Bambalapitiya
        double distance = calculator.calculateDistanceKm(6.9344, 79.8428, 6.8930, 79.8550);
        assertTrue(distance > 3.0 && distance < 6.0,
                "Expected short distance ~4.5 km, but got: " + distance);
    }
}

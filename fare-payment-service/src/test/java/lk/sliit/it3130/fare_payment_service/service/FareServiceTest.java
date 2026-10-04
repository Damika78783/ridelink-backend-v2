package lk.sliit.it3130.fare_payment_service.service;

import lk.sliit.it3130.fare_payment_service.dto.FareCalculationResult;
import lk.sliit.it3130.fare_payment_service.dto.FareEstimateRequest;
import lk.sliit.it3130.fare_payment_service.dto.FareEstimateResponse;
import lk.sliit.it3130.fare_payment_service.model.VehicleType;
import lk.sliit.it3130.fare_payment_service.service.fare.FareCalculationStrategy;
import lk.sliit.it3130.fare_payment_service.service.fare.HaversineDistanceCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FareServiceTest {

    @Mock
    private FareCalculationStrategy fareCalculationStrategy;

    @Mock
    private HaversineDistanceCalculator haversineDistanceCalculator;

    @InjectMocks
    private FareServiceImpl fareService;

    @Test
    @DisplayName("Should orchestrate fare estimation end-to-end")
    void testEstimateFare() {
        FareEstimateRequest request = FareEstimateRequest.builder()
                .pickupLatitude(6.9271)
                .pickupLongitude(79.8612)
                .destinationLatitude(6.9344)
                .destinationLongitude(79.8428)
                .vehicleType(VehicleType.CAR)
                .estimatedDurationMin(15.0)
                .build();

        when(haversineDistanceCalculator.calculateDistanceKm(6.9271, 79.8612, 6.9344, 79.8428))
                .thenReturn(4.5);

        FareCalculationResult mockResult = FareCalculationResult.builder()
                .distanceKm(4.5)
                .durationMinutes(15.0)
                .vehicleType(VehicleType.CAR)
                .baseFare(BigDecimal.valueOf(150.00))
                .distanceFare(BigDecimal.valueOf(360.00))
                .timeFare(BigDecimal.valueOf(75.00))
                .vehicleMultiplier(1.2)
                .subtotal(BigDecimal.valueOf(585.00))
                .totalFare(BigDecimal.valueOf(702.00))
                .minimumFare(BigDecimal.valueOf(200.00))
                .currency("LKR")
                .build();

        when(fareCalculationStrategy.calculateFare(4.5, 15.0, VehicleType.CAR))
                .thenReturn(mockResult);

        FareEstimateResponse response = fareService.estimateFare(request);

        assertNotNull(response);
        assertEquals(4.5, response.getDistanceKm());
        assertEquals(15.0, response.getEstimatedDurationMin());
        assertEquals(BigDecimal.valueOf(702.00), response.getEstimatedFare());
        assertEquals("LKR", response.getCurrency());

        verify(haversineDistanceCalculator).calculateDistanceKm(6.9271, 79.8612, 6.9344, 79.8428);
        verify(fareCalculationStrategy).calculateFare(4.5, 15.0, VehicleType.CAR);
    }

    @Test
    @DisplayName("Should compute estimated duration in minutes when not provided in request")
    void testEstimateDurationMinutes() {
        // 25 km at 25 km/h = 1 hour = 60 minutes
        double duration = fareService.estimateDurationMinutes(25.0);
        assertEquals(60.0, duration, 0.1);
    }
}

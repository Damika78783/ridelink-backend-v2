package lk.sliit.it3130.fare_payment_service.controller;

import lk.sliit.it3130.fare_payment_service.dto.FareEstimateRequest;
import lk.sliit.it3130.fare_payment_service.dto.FareEstimateResponse;
import lk.sliit.it3130.fare_payment_service.model.VehicleType;
import lk.sliit.it3130.fare_payment_service.service.FareService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FareControllerTest {

    @Mock
    private FareService fareService;

    @InjectMocks
    private FareController fareController;

    @Test
    @DisplayName("Should return 200 OK with FareEstimateResponse")
    void testEstimateFareEndpoint() {
        FareEstimateRequest request = FareEstimateRequest.builder()
                .pickupLatitude(6.9271)
                .pickupLongitude(79.8612)
                .destinationLatitude(6.9344)
                .destinationLongitude(79.8428)
                .vehicleType(VehicleType.CAR)
                .build();

        FareEstimateResponse mockResponse = FareEstimateResponse.builder()
                .distanceKm(4.5)
                .estimatedDurationMin(15.0)
                .vehicleType(VehicleType.CAR)
                .estimatedFare(BigDecimal.valueOf(702.00))
                .currency("LKR")
                .build();

        when(fareService.estimateFare(any(FareEstimateRequest.class))).thenReturn(mockResponse);

        ResponseEntity<FareEstimateResponse> responseEntity = fareController.estimateFare(request);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(BigDecimal.valueOf(702.00), responseEntity.getBody().getEstimatedFare());
        verify(fareService).estimateFare(request);
    }
}

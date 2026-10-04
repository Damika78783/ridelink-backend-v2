package lk.sliit.it3130.fare_payment_service.client;

import lk.sliit.it3130.fare_payment_service.dto.RideResponse;
import lk.sliit.it3130.fare_payment_service.exception.ExternalServiceException;
import lk.sliit.it3130.fare_payment_service.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class RestRideClient implements RideClient {

    private final RestTemplate restTemplate;
    private final String rideServiceBaseUrl;

    public RestRideClient(RestTemplate restTemplate,
                          @Value("${services.ride-service.base-url:http://localhost:8082}") String rideServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.rideServiceBaseUrl = rideServiceBaseUrl;
    }

    @Override
    public RideResponse getRideById(Long rideId) {
        if (rideId == null) {
            throw new IllegalArgumentException("Ride ID cannot be null");
        }

        String url = rideServiceBaseUrl + "/api/rides/" + rideId;

        try {
            ResponseEntity<RideResponse> response = restTemplate.getForEntity(url, RideResponse.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            throw new ResourceNotFoundException("Ride not found with id: " + rideId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Ride not found with id: " + rideId);
        } catch (RestClientException e) {
            throw new ExternalServiceException("Failed to communicate with Ride Management Service: " + e.getMessage(), e);
        }
    }
}

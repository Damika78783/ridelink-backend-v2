package lk.sliit.it3130.fare_payment_service.client;

import lk.sliit.it3130.fare_payment_service.dto.RideResponse;

public interface RideClient {

    /**
     * Fetches ride details from the Ride Management Service.
     *
     * @param rideId Identifier of the ride
     * @return RideResponse with ride status, coordinates, vehicle type, and duration
     */
    RideResponse getRideById(Long rideId);
}

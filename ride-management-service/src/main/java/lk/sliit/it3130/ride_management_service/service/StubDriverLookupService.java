package lk.sliit.it3130.ride_management_service.service;

import org.springframework.stereotype.Service;

@Service
public class StubDriverLookupService implements DriverLookupService {

    // Hardcoded fake driver — placeholder until Driver & Vehicle Service exists
    private static final String STUB_DRIVER_EMAIL = "driver1@ridelink.com";

    @Override
    public String findEligibleDriver(String pickupLocation) {
        // In future: call Driver & Vehicle Service's "eligible drivers" endpoint
        // and pick one based on pickupLocation/service area.
        return STUB_DRIVER_EMAIL;
    }
}
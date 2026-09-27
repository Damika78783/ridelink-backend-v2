package lk.sliit.it3130.ride_management_service.service;

public interface DriverLookupService {

    /**
     * Returns the email of an eligible available driver, or null if none available.
     * Stubbed for now — will be replaced with a real HTTP call to
     * Driver & Vehicle Service once that service exists.
     */
    String findEligibleDriver(String pickupLocation);
}
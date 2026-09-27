package lk.sliit.it3130.ride_management_service.service;

import lk.sliit.it3130.ride_management_service.dto.RideRequest;
import lk.sliit.it3130.ride_management_service.dto.RideResponse;
import lk.sliit.it3130.ride_management_service.exception.InvalidStatusTransitionException;
import lk.sliit.it3130.ride_management_service.exception.RideNotFoundException;
import lk.sliit.it3130.ride_management_service.model.Ride;
import lk.sliit.it3130.ride_management_service.model.RideStatus;
import lk.sliit.it3130.ride_management_service.repository.RideRepository;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final DriverLookupService driverLookupService;

    // Valid status transition map — defines which states each state can move to
    private static final Map<RideStatus, EnumSet<RideStatus>> VALID_TRANSITIONS = new EnumMap<>(RideStatus.class);

    static {
        VALID_TRANSITIONS.put(RideStatus.REQUESTED, EnumSet.of(RideStatus.ASSIGNED, RideStatus.CANCELLED));
        VALID_TRANSITIONS.put(RideStatus.ASSIGNED, EnumSet.of(RideStatus.ACCEPTED, RideStatus.CANCELLED));
        VALID_TRANSITIONS.put(RideStatus.ACCEPTED, EnumSet.of(RideStatus.IN_PROGRESS, RideStatus.CANCELLED));
        VALID_TRANSITIONS.put(RideStatus.IN_PROGRESS, EnumSet.of(RideStatus.COMPLETED, RideStatus.CANCELLED));
        VALID_TRANSITIONS.put(RideStatus.COMPLETED, EnumSet.noneOf(RideStatus.class)); // terminal
        VALID_TRANSITIONS.put(RideStatus.CANCELLED, EnumSet.noneOf(RideStatus.class)); // terminal
    }

    public RideService(RideRepository rideRepository, DriverLookupService driverLookupService) {
        this.rideRepository = rideRepository;
        this.driverLookupService = driverLookupService;
    }

    public RideResponse createRide(String passengerEmail, RideRequest request) {
        Ride ride = new Ride();
        ride.setPassengerEmail(passengerEmail);
        ride.setPickupLocation(request.getPickupLocation());
        ride.setDestinationLocation(request.getDestinationLocation());
        ride.setStatus(RideStatus.REQUESTED);

        Ride saved = rideRepository.save(ride);
        return toResponse(saved);
    }

    public RideResponse getRide(Long id) {
        Ride ride = findRideOrThrow(id);
        return toResponse(ride);
    }

    public List<RideResponse> getRidesByPassenger(String passengerEmail) {
        return rideRepository.findByPassengerEmail(passengerEmail)
                .stream().map(this::toResponse).toList();
    }

    public List<RideResponse> getRidesByDriver(String driverEmail) {
        return rideRepository.findByDriverEmail(driverEmail)
                .stream().map(this::toResponse).toList();
    }

    public RideResponse assignDriver(Long id) {
        Ride ride = findRideOrThrow(id);

        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new InvalidStatusTransitionException(
                    "Cannot assign a driver to a ride in status " + ride.getStatus());
        }

        String driverEmail = driverLookupService.findEligibleDriver(ride.getPickupLocation());
        if (driverEmail == null) {
            throw new InvalidStatusTransitionException("No available driver found for this pickup location");
        }

        ride.setDriverEmail(driverEmail);
        ride.setStatus(RideStatus.ASSIGNED);

        Ride saved = rideRepository.save(ride);
        return toResponse(saved);
    }

    public RideResponse updateStatus(Long id, RideStatus newStatus) {
        Ride ride = findRideOrThrow(id);
        RideStatus currentStatus = ride.getStatus();

        EnumSet<RideStatus> allowedNextStates = VALID_TRANSITIONS.get(currentStatus);
        if (allowedNextStates == null || !allowedNextStates.contains(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Cannot transition ride from " + currentStatus + " to " + newStatus);
        }

        ride.setStatus(newStatus);
        Ride saved = rideRepository.save(ride);
        return toResponse(saved);
    }

    private Ride findRideOrThrow(Long id) {
        return rideRepository.findById(id)
                .orElseThrow(() -> new RideNotFoundException("Ride not found with id: " + id));
    }

    private RideResponse toResponse(Ride ride) {
        return new RideResponse(
                ride.getId(),
                ride.getPassengerEmail(),
                ride.getDriverEmail(),
                ride.getPickupLocation(),
                ride.getDestinationLocation(),
                ride.getStatus(),
                ride.getRequestedAt(),
                ride.getUpdatedAt()
        );
    }
}
package lk.sliit.it3130.ride_management_service.service;

import lk.sliit.it3130.ride_management_service.dto.RideRequest;
import lk.sliit.it3130.ride_management_service.dto.RideResponse;
import lk.sliit.it3130.ride_management_service.exception.InvalidStatusTransitionException;
import lk.sliit.it3130.ride_management_service.exception.RideNotFoundException;
import lk.sliit.it3130.ride_management_service.model.Ride;
import lk.sliit.it3130.ride_management_service.model.RideStatus;
import lk.sliit.it3130.ride_management_service.repository.RideRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock private RideRepository rideRepository;
    @Mock private DriverLookupService driverLookupService;

    @InjectMocks private RideService rideService;

    // ---------- helpers ----------

    private Ride ride(Long id, RideStatus status) {
        Ride r = new Ride();
        r.setId(id);
        r.setPassengerEmail("passenger@test.com");
        r.setPickupLocation("Colombo Fort");
        r.setDestinationLocation("Kandy");
        r.setStatus(status);
        return r;
    }

    private RideRequest request(String pickup, String destination) {
        RideRequest req = new RideRequest();
        req.setPickupLocation(pickup);
        req.setDestinationLocation(destination);
        return req;
    }

    private void saveReturnsArgument() {
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // Mirror of the rules documented in RideService — the test is the independent specification.
    private static final Map<RideStatus, Set<RideStatus>> ALLOWED = Map.of(
            RideStatus.REQUESTED, Set.of(RideStatus.ASSIGNED, RideStatus.CANCELLED),
            RideStatus.ASSIGNED, Set.of(RideStatus.ACCEPTED, RideStatus.CANCELLED),
            RideStatus.ACCEPTED, Set.of(RideStatus.IN_PROGRESS, RideStatus.CANCELLED),
            RideStatus.IN_PROGRESS, Set.of(RideStatus.COMPLETED, RideStatus.CANCELLED),
            RideStatus.COMPLETED, Set.of(),
            RideStatus.CANCELLED, Set.of());

    static Stream<Arguments> validTransitions() {
        List<Arguments> out = new ArrayList<>();
        ALLOWED.forEach((from, tos) -> tos.forEach(to -> out.add(Arguments.of(from, to))));
        return out.stream();
    }

    static Stream<Arguments> invalidTransitions() {
        List<Arguments> out = new ArrayList<>();
        for (RideStatus from : RideStatus.values()) {
            for (RideStatus to : RideStatus.values()) {
                if (!ALLOWED.get(from).contains(to)) {
                    out.add(Arguments.of(from, to));
                }
            }
        }
        return out.stream();
    }

    // ---------- createRide ----------

    @Test
    @DisplayName("createRide: saves a REQUESTED ride owned by the given passenger")
    void createRide_success() {
        saveReturnsArgument();

        RideResponse response = rideService.createRide("passenger@test.com", request("Galle", "Matara"));

        ArgumentCaptor<Ride> captor = ArgumentCaptor.forClass(Ride.class);
        verify(rideRepository).save(captor.capture());
        Ride saved = captor.getValue();
        assertEquals("passenger@test.com", saved.getPassengerEmail());
        assertEquals("Galle", saved.getPickupLocation());
        assertEquals("Matara", saved.getDestinationLocation());
        assertEquals(RideStatus.REQUESTED, saved.getStatus());
        assertNull(saved.getDriverEmail());

        assertEquals(RideStatus.REQUESTED, response.getStatus());
        assertEquals("Galle", response.getPickupLocation());
        assertNull(response.getDriverEmail());
    }

    // ---------- getRide ----------

    @Test
    @DisplayName("getRide: returns mapped response when ride exists")
    void getRide_found() {
        when(rideRepository.findById(5L)).thenReturn(Optional.of(ride(5L, RideStatus.ASSIGNED)));

        RideResponse response = rideService.getRide(5L);

        assertEquals(5L, response.getId());
        assertEquals(RideStatus.ASSIGNED, response.getStatus());
        assertEquals("passenger@test.com", response.getPassengerEmail());
    }

    @Test
    @DisplayName("getRide: throws RideNotFoundException when ride does not exist")
    void getRide_notFound() {
        when(rideRepository.findById(99L)).thenReturn(Optional.empty());

        RideNotFoundException ex = assertThrows(RideNotFoundException.class, () -> rideService.getRide(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    // ---------- listing ----------

    @Test
    @DisplayName("getRidesByPassenger: returns all rides of that passenger")
    void getRidesByPassenger_returnsRides() {
        when(rideRepository.findByPassengerEmail("passenger@test.com"))
                .thenReturn(List.of(ride(1L, RideStatus.REQUESTED), ride(2L, RideStatus.COMPLETED)));

        List<RideResponse> result = rideService.getRidesByPassenger("passenger@test.com");

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(RideStatus.COMPLETED, result.get(1).getStatus());
    }

    @Test
    @DisplayName("getRidesByPassenger: returns empty list when passenger has no rides")
    void getRidesByPassenger_empty() {
        when(rideRepository.findByPassengerEmail("none@test.com")).thenReturn(List.of());

        assertTrue(rideService.getRidesByPassenger("none@test.com").isEmpty());
    }

    @Test
    @DisplayName("getRidesByDriver: returns rides assigned to that driver")
    void getRidesByDriver_returnsRides() {
        Ride r = ride(3L, RideStatus.ACCEPTED);
        r.setDriverEmail("driver@test.com");
        when(rideRepository.findByDriverEmail("driver@test.com")).thenReturn(List.of(r));

        List<RideResponse> result = rideService.getRidesByDriver("driver@test.com");

        assertEquals(1, result.size());
        assertEquals("driver@test.com", result.get(0).getDriverEmail());
    }

    // ---------- assignDriver ----------

    @Test
    @DisplayName("assignDriver: assigns eligible driver and moves ride to ASSIGNED")
    void assignDriver_success() {
        Ride r = ride(1L, RideStatus.REQUESTED);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(r));
        when(driverLookupService.findEligibleDriver("Colombo Fort")).thenReturn("driver@test.com");
        saveReturnsArgument();

        RideResponse response = rideService.assignDriver(1L);

        assertEquals(RideStatus.ASSIGNED, response.getStatus());
        assertEquals("driver@test.com", response.getDriverEmail());
        verify(rideRepository).save(r);
    }

    @Test
    @DisplayName("assignDriver (negative): no available driver -> exception, ride NOT saved")
    void assignDriver_noDriverAvailable() {
        Ride r = ride(1L, RideStatus.REQUESTED);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(r));
        when(driverLookupService.findEligibleDriver("Colombo Fort")).thenReturn(null);

        InvalidStatusTransitionException ex = assertThrows(InvalidStatusTransitionException.class,
                () -> rideService.assignDriver(1L));

        assertTrue(ex.getMessage().contains("No available driver"));
        assertEquals(RideStatus.REQUESTED, r.getStatus());
        assertNull(r.getDriverEmail());
        verify(rideRepository, never()).save(any());
    }

    @ParameterizedTest(name = "assignDriver (negative): ride in {0} cannot be assigned")
    @EnumSource(value = RideStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "REQUESTED")
    void assignDriver_wrongStatus(RideStatus status) {
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride(1L, status)));

        InvalidStatusTransitionException ex = assertThrows(InvalidStatusTransitionException.class,
                () -> rideService.assignDriver(1L));

        assertTrue(ex.getMessage().contains(status.name()));
        verifyNoInteractions(driverLookupService);
        verify(rideRepository, never()).save(any());
    }

    @Test
    @DisplayName("assignDriver (negative): unknown ride id -> RideNotFoundException")
    void assignDriver_rideNotFound() {
        when(rideRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(RideNotFoundException.class, () -> rideService.assignDriver(404L));
        verifyNoInteractions(driverLookupService);
    }

    // ---------- updateStatus ----------

    @ParameterizedTest(name = "updateStatus: {0} -> {1} is allowed")
    @MethodSource("validTransitions")
    void updateStatus_validTransition(RideStatus from, RideStatus to) {
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride(1L, from)));
        saveReturnsArgument();

        RideResponse response = rideService.updateStatus(1L, to);

        assertEquals(to, response.getStatus());
        verify(rideRepository).save(any(Ride.class));
    }

    @ParameterizedTest(name = "updateStatus (negative): {0} -> {1} is rejected")
    @MethodSource("invalidTransitions")
    void updateStatus_invalidTransition(RideStatus from, RideStatus to) {
        Ride r = ride(1L, from);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(r));

        InvalidStatusTransitionException ex = assertThrows(InvalidStatusTransitionException.class,
                () -> rideService.updateStatus(1L, to));

        assertTrue(ex.getMessage().contains(from.name()));
        assertTrue(ex.getMessage().contains(to.name()));
        assertEquals(from, r.getStatus(), "ride status must remain unchanged");
        verify(rideRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus (negative): unknown ride id -> RideNotFoundException")
    void updateStatus_rideNotFound() {
        when(rideRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(RideNotFoundException.class, () -> rideService.updateStatus(404L, RideStatus.CANCELLED));
        verify(rideRepository, never()).save(any());
    }
}

package lk.sliit.it3130.ride_management_service.controller;

import jakarta.validation.Valid;
import lk.sliit.it3130.ride_management_service.dto.RideRequest;
import lk.sliit.it3130.ride_management_service.dto.RideResponse;
import lk.sliit.it3130.ride_management_service.dto.StatusUpdateRequest;
import lk.sliit.it3130.ride_management_service.service.RideService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    // Create a ride request.
    // The passenger's identity is taken from the JWT (authentication.getName()),
    // NEVER from the request body — a client must not be able to book a ride "as" someone else.
    @PostMapping
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody RideRequest request,
                                                   Authentication authentication) {
        String passengerEmail = authentication.getName();
        RideResponse response = rideService.createRide(passengerEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Retrieve a single ride — any authenticated user of the platform
    @GetMapping("/{id}")
    public ResponseEntity<RideResponse> getRide(@PathVariable Long id) {
        return ResponseEntity.ok(rideService.getRide(id));
    }

    // A passenger may only list their own rides; an admin may list anyone's
    @GetMapping("/passenger/{email}")
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    public ResponseEntity<List<RideResponse>> getRidesByPassenger(@PathVariable String email) {
        return ResponseEntity.ok(rideService.getRidesByPassenger(email));
    }

    // A driver may only list their own assigned rides; an admin may list anyone's
    @GetMapping("/driver/{email}")
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    public ResponseEntity<List<RideResponse>> getRidesByDriver(@PathVariable String email) {
        return ResponseEntity.ok(rideService.getRidesByDriver(email));
    }

    // Assign an eligible driver to a REQUESTED ride.
    // Driver selection is delegated to DriverLookupService (currently stubbed,
    // later replaced by a call to the Driver & Vehicle Service).
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    public ResponseEntity<RideResponse> assignDriver(@PathVariable Long id) {
        return ResponseEntity.ok(rideService.assignDriver(id));
    }

    // Move the ride through its lifecycle (accept / start / complete / cancel).
    // Transition validity is enforced in RideService, not here.
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<RideResponse> updateStatus(@PathVariable Long id,
                                                      @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(rideService.updateStatus(id, request.getStatus()));
    }
}
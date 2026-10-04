package com.ridelink.drivervehicle.controller;

import com.ridelink.drivervehicle.dto.request.CreateDriverRequest;
import com.ridelink.drivervehicle.dto.request.UpdateAvailabilityRequest;
import com.ridelink.drivervehicle.dto.request.UpdateDriverRequest;
import com.ridelink.drivervehicle.dto.request.UpdateLocationRequest;
import com.ridelink.drivervehicle.dto.request.UpdateServiceAreaRequest;
import com.ridelink.drivervehicle.dto.response.AvailableDriverResponse;
import com.ridelink.drivervehicle.dto.response.DriverResponse;
import com.ridelink.drivervehicle.dto.response.ErrorResponse;
import com.ridelink.drivervehicle.model.VehicleType;
import com.ridelink.drivervehicle.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Tag(name = "Driver Management", description = "Endpoints for managing driver operational profiles, availability, and simulated locations")
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    @Operation(summary = "Create driver operational profile", description = "Registers an operational driver profile linked to an existing Account Service account ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Driver operational profile successfully created",
                    content = @Content(schema = @Schema(implementation = DriverResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Duplicate driver profile (account ID or license number already exists)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DriverResponse> createDriver(
            @Valid @RequestBody CreateDriverRequest request) {
        log.info("REST request to create driver profile for accountId: {}", request.getAccountId());
        DriverResponse response = driverService.createDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get driver profile by ID", description = "Retrieves complete driver profile details including associated vehicle.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DriverResponse.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DriverResponse> getDriverById(
            @Parameter(description = "Driver Profile ID", example = "66f42a1b9c3e4a2d8f1e5a7b")
            @PathVariable String id) {
        log.info("REST request to get driver profile by id: {}", id);
        return ResponseEntity.ok(driverService.getDriverById(id));
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get driver profile by Account ID", description = "Helper endpoint to retrieve a driver profile using the Account Service account identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DriverResponse.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found for given account ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DriverResponse> getDriverByAccountId(
            @Parameter(description = "Account ID", example = "acc-60d5ecb8b39f")
            @PathVariable String accountId) {
        log.info("REST request to get driver profile by accountId: {}", accountId);
        return ResponseEntity.ok(driverService.getDriverByAccountId(accountId));
    }

    @GetMapping
    @Operation(summary = "List all drivers", description = "Retrieves all driver operational profiles.")
    @ApiResponse(responseCode = "200", description = "Drivers retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DriverResponse.class))))
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update driver operational profile", description = "Updates general operational details such as license number or service area.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver updated successfully",
                    content = @Content(schema = @Schema(implementation = DriverResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "License number already in use",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DriverResponse> updateDriver(
            @Parameter(description = "Driver Profile ID", example = "66f42a1b9c3e4a2d8f1e5a7b")
            @PathVariable String id,
            @Valid @RequestBody UpdateDriverRequest request) {
        log.info("REST request to update driver id: {}", id);
        return ResponseEntity.ok(driverService.updateDriver(id, request));
    }

    @PutMapping("/{id}/availability")
    @Operation(summary = "Update driver availability status", description = "Updates operational availability (AVAILABLE, UNAVAILABLE, ON_TRIP, OFFLINE). Requires a registered vehicle to become AVAILABLE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability status updated successfully",
                    content = @Content(schema = @Schema(implementation = DriverResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status or driver has no registered vehicle",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DriverResponse> updateAvailability(
            @Parameter(description = "Driver Profile ID", example = "66f42a1b9c3e4a2d8f1e5a7b")
            @PathVariable String id,
            @Valid @RequestBody UpdateAvailabilityRequest request) {
        log.info("REST request to update availability for driver id: {} to {}", id, request.getAvailabilityStatus());
        return ResponseEntity.ok(driverService.updateAvailability(id, request));
    }

    @PutMapping("/{id}/location")
    @Operation(summary = "Update driver simulated location", description = "Updates the driver's current simulated latitude, longitude, and landmark.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulated location updated successfully",
                    content = @Content(schema = @Schema(implementation = DriverResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid coordinate ranges",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DriverResponse> updateLocation(
            @Parameter(description = "Driver Profile ID", example = "66f42a1b9c3e4a2d8f1e5a7b")
            @PathVariable String id,
            @Valid @RequestBody UpdateLocationRequest request) {
        log.info("REST request to update location for driver id: {}", id);
        return ResponseEntity.ok(driverService.updateLocation(id, request));
    }

    @PutMapping("/{id}/service-area")
    @Operation(summary = "Update driver service area", description = "Updates the operational zone/city for the driver.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service area updated successfully",
                    content = @Content(schema = @Schema(implementation = DriverResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid service area value",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DriverResponse> updateServiceArea(
            @Parameter(description = "Driver Profile ID", example = "66f42a1b9c3e4a2d8f1e5a7b")
            @PathVariable String id,
            @Valid @RequestBody UpdateServiceAreaRequest request) {
        log.info("REST request to update service area for driver id: {}", id);
        return ResponseEntity.ok(driverService.updateServiceArea(id, request));
    }

    @GetMapping("/available")
    @Operation(summary = "Retrieve eligible available drivers",
            description = "Primary integration endpoint consumed by Ride Management Service. "
                    + "Returns eligible drivers who are currently AVAILABLE, have a registered vehicle, "
                    + "and match optional serviceArea, vehicleType, and proximity criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eligible available drivers retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AvailableDriverResponse.class))))
    })
    public ResponseEntity<List<AvailableDriverResponse>> getAvailableDrivers(
            @Parameter(description = "Filter by service area (e.g. Colombo, Kandy)", example = "Colombo")
            @RequestParam(required = false) String serviceArea,

            @Parameter(description = "Filter by vehicle type (CAR, VAN, MOTORBIKE, TUK_TUK, SUV)", example = "CAR")
            @RequestParam(required = false) VehicleType vehicleType,

            @Parameter(description = "Pickup latitude for proximity calculation", example = "6.9271")
            @RequestParam(required = false) Double latitude,

            @Parameter(description = "Pickup longitude for proximity calculation", example = "79.8612")
            @RequestParam(required = false) Double longitude,

            @Parameter(description = "Maximum search radius in kilometers (default: 25.0 km)", example = "10.0")
            @RequestParam(required = false) Double radiusKm) {

        log.info("REST request to fetch available drivers - serviceArea: {}, vehicleType: {}, lat: {}, lon: {}, radius: {}",
                serviceArea, vehicleType, latitude, longitude, radiusKm);

        List<AvailableDriverResponse> availableDrivers = driverService.getAvailableDrivers(
                serviceArea, vehicleType, latitude, longitude, radiusKm);

        return ResponseEntity.ok(availableDrivers);
    }
}

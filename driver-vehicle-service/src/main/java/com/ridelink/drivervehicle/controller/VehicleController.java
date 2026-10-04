package com.ridelink.drivervehicle.controller;

import com.ridelink.drivervehicle.dto.request.CreateVehicleRequest;
import com.ridelink.drivervehicle.dto.request.UpdateVehicleRequest;
import com.ridelink.drivervehicle.dto.response.ErrorResponse;
import com.ridelink.drivervehicle.dto.response.VehicleResponse;
import com.ridelink.drivervehicle.service.VehicleService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicle Management", description = "Endpoints for registering and maintaining vehicles associated with drivers")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @Operation(summary = "Register vehicle for a driver", description = "Registers vehicle details and assigns it to a registered driver profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehicle successfully registered",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or driver already has a vehicle",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Vehicle registration number already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VehicleResponse> createVehicle(
            @Valid @RequestBody CreateVehicleRequest request) {
        log.info("REST request to register vehicle with plate: {} for driver: {}",
                request.getVehicleNumber(), request.getDriverId());
        VehicleResponse response = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID", description = "Retrieves vehicle details by its unique identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VehicleResponse> getVehicleById(
            @Parameter(description = "Vehicle ID", example = "66f42b3c9c3e4a2d8f1e5a7c")
            @PathVariable String id) {
        log.info("REST request to get vehicle by id: {}", id);
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get vehicle by Driver ID", description = "Retrieves the vehicle registered to a specific driver.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle retrieved successfully",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found for driver",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VehicleResponse> getVehicleByDriverId(
            @Parameter(description = "Driver Profile ID", example = "66f42a1b9c3e4a2d8f1e5a7b")
            @PathVariable String driverId) {
        log.info("REST request to get vehicle for driverId: {}", driverId);
        return ResponseEntity.ok(vehicleService.getVehicleByDriverId(driverId));
    }

    @GetMapping
    @Operation(summary = "List all vehicles", description = "Retrieves all registered vehicles in the system.")
    @ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = VehicleResponse.class))))
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle details", description = "Updates details such as registration number, vehicle category, or model.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle updated successfully",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Vehicle registration number already in use",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VehicleResponse> updateVehicle(
            @Parameter(description = "Vehicle ID", example = "66f42b3c9c3e4a2d8f1e5a7c")
            @PathVariable String id,
            @Valid @RequestBody UpdateVehicleRequest request) {
        log.info("REST request to update vehicle id: {}", id);
        return ResponseEntity.ok(vehicleService.updateVehicle(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle", description = "Removes a vehicle record by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vehicle successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteVehicle(
            @Parameter(description = "Vehicle ID", example = "66f42b3c9c3e4a2d8f1e5a7c")
            @PathVariable String id) {
        log.info("REST request to delete vehicle id: {}", id);
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}

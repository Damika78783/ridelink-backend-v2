package com.ridelink.drivervehicle.service;

import com.ridelink.drivervehicle.dto.request.CreateVehicleRequest;
import com.ridelink.drivervehicle.dto.request.UpdateVehicleRequest;
import com.ridelink.drivervehicle.dto.response.VehicleResponse;
import com.ridelink.drivervehicle.exception.BadRequestException;
import com.ridelink.drivervehicle.exception.DuplicateResourceException;
import com.ridelink.drivervehicle.exception.ResourceNotFoundException;
import com.ridelink.drivervehicle.model.Vehicle;
import com.ridelink.drivervehicle.repository.DriverRepository;
import com.ridelink.drivervehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    @Override
    @Transactional
    public VehicleResponse createVehicle(CreateVehicleRequest request) {
        log.info("Creating vehicle with number: {} for driver: {}", request.getVehicleNumber(), request.getDriverId());

        // Validate driver existence
        if (!driverRepository.existsById(request.getDriverId())) {
            throw new ResourceNotFoundException("Driver not found with id: " + request.getDriverId());
        }

        // Validate vehicle number uniqueness
        if (vehicleRepository.existsByVehicleNumber(request.getVehicleNumber())) {
            throw new DuplicateResourceException("Vehicle with registration number '" + request.getVehicleNumber() + "' already exists");
        }

        // Validate driver does not already have an assigned vehicle
        if (vehicleRepository.existsByDriverId(request.getDriverId())) {
            throw new BadRequestException("Driver with id '" + request.getDriverId() + "' already has a registered vehicle");
        }

        Vehicle vehicle = Vehicle.builder()
                .driverId(request.getDriverId())
                .vehicleNumber(request.getVehicleNumber().trim().toUpperCase())
                .vehicleType(request.getVehicleType())
                .model(request.getModel().trim())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle successfully registered with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    public VehicleResponse getVehicleById(String id) {
        log.info("Fetching vehicle by id: {}", id);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
        return mapToResponse(vehicle);
    }

    @Override
    public VehicleResponse getVehicleByDriverId(String driverId) {
        log.info("Fetching vehicle for driver id: {}", driverId);
        Vehicle vehicle = vehicleRepository.findByDriverId(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("No vehicle registered for driver id: " + driverId));
        return mapToResponse(vehicle);
    }

    @Override
    @Transactional
    public VehicleResponse updateVehicle(String id, UpdateVehicleRequest request) {
        log.info("Updating vehicle id: {}", id);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        if (request.getVehicleNumber() != null && !request.getVehicleNumber().isBlank()) {
            String newNumber = request.getVehicleNumber().trim().toUpperCase();
            if (!newNumber.equalsIgnoreCase(vehicle.getVehicleNumber())
                    && vehicleRepository.existsByVehicleNumber(newNumber)) {
                throw new DuplicateResourceException("Vehicle with registration number '" + newNumber + "' already exists");
            }
            vehicle.setVehicleNumber(newNumber);
        }

        if (request.getVehicleType() != null) {
            vehicle.setVehicleType(request.getVehicleType());
        }

        if (request.getModel() != null && !request.getModel().isBlank()) {
            vehicle.setModel(request.getModel().trim());
        }

        vehicle.setUpdatedAt(LocalDateTime.now());
        Vehicle updated = vehicleRepository.save(vehicle);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteVehicle(String id) {
        log.info("Deleting vehicle id: {}", id);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
        vehicleRepository.delete(vehicle);
    }

    @Override
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private VehicleResponse mapToResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .driverId(vehicle.getDriverId())
                .vehicleNumber(vehicle.getVehicleNumber())
                .vehicleType(vehicle.getVehicleType())
                .model(vehicle.getModel())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}

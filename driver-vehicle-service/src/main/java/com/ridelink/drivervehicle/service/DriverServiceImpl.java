package com.ridelink.drivervehicle.service;

import com.ridelink.drivervehicle.dto.request.CreateDriverRequest;
import com.ridelink.drivervehicle.dto.request.UpdateAvailabilityRequest;
import com.ridelink.drivervehicle.dto.request.UpdateDriverRequest;
import com.ridelink.drivervehicle.dto.request.UpdateLocationRequest;
import com.ridelink.drivervehicle.dto.request.UpdateServiceAreaRequest;
import com.ridelink.drivervehicle.dto.response.AvailableDriverResponse;
import com.ridelink.drivervehicle.dto.response.DriverResponse;
import com.ridelink.drivervehicle.dto.response.LocationResponse;
import com.ridelink.drivervehicle.dto.response.VehicleResponse;
import com.ridelink.drivervehicle.exception.BadRequestException;
import com.ridelink.drivervehicle.exception.DuplicateResourceException;
import com.ridelink.drivervehicle.exception.ResourceNotFoundException;
import com.ridelink.drivervehicle.model.AvailabilityStatus;
import com.ridelink.drivervehicle.model.Driver;
import com.ridelink.drivervehicle.model.Location;
import com.ridelink.drivervehicle.model.Vehicle;
import com.ridelink.drivervehicle.model.VehicleType;
import com.ridelink.drivervehicle.repository.DriverRepository;
import com.ridelink.drivervehicle.repository.VehicleRepository;
import com.ridelink.drivervehicle.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public DriverResponse createDriver(CreateDriverRequest request) {
        log.info("Creating driver operational profile for accountId: {}", request.getAccountId());

        if (driverRepository.existsByAccountId(request.getAccountId())) {
            throw new DuplicateResourceException("A driver profile already exists for accountId: " + request.getAccountId());
        }

        String normalizedLicense = request.getLicenseNumber().trim().toUpperCase();
        if (driverRepository.existsByLicenseNumber(normalizedLicense)) {
            throw new DuplicateResourceException("A driver profile already exists with license number: " + normalizedLicense);
        }

        Location initialLocation = null;
        if (request.getInitialLatitude() != null && request.getInitialLongitude() != null) {
            initialLocation = Location.builder()
                    .latitude(request.getInitialLatitude())
                    .longitude(request.getInitialLongitude())
                    .address(request.getInitialAddress())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        AvailabilityStatus status = request.getAvailabilityStatus() != null
                ? request.getAvailabilityStatus()
                : AvailabilityStatus.OFFLINE;

        Driver driver = Driver.builder()
                .accountId(request.getAccountId().trim())
                .licenseNumber(normalizedLicense)
                .serviceArea(request.getServiceArea().trim())
                .availabilityStatus(status)
                .currentLocation(initialLocation)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Driver saved = driverRepository.save(driver);
        log.info("Driver profile created successfully with id: {}", saved.getId());
        return mapToDriverResponse(saved, null);
    }

    @Override
    public DriverResponse getDriverById(String id) {
        log.info("Fetching driver by id: {}", id);
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));

        Optional<Vehicle> vehicleOpt = vehicleRepository.findByDriverId(driver.getId());
        return mapToDriverResponse(driver, vehicleOpt.orElse(null));
    }

    @Override
    public DriverResponse getDriverByAccountId(String accountId) {
        log.info("Fetching driver by accountId: {}", accountId);
        Driver driver = driverRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with accountId: " + accountId));

        Optional<Vehicle> vehicleOpt = vehicleRepository.findByDriverId(driver.getId());
        return mapToDriverResponse(driver, vehicleOpt.orElse(null));
    }

    @Override
    @Transactional
    public DriverResponse updateDriver(String id, UpdateDriverRequest request) {
        log.info("Updating general driver profile for id: {}", id);
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));

        if (request.getLicenseNumber() != null && !request.getLicenseNumber().isBlank()) {
            String newLicense = request.getLicenseNumber().trim().toUpperCase();
            if (!newLicense.equalsIgnoreCase(driver.getLicenseNumber())
                    && driverRepository.existsByLicenseNumber(newLicense)) {
                throw new DuplicateResourceException("License number '" + newLicense + "' is already in use by another driver");
            }
            driver.setLicenseNumber(newLicense);
        }

        if (request.getServiceArea() != null && !request.getServiceArea().isBlank()) {
            driver.setServiceArea(request.getServiceArea().trim());
        }

        driver.setUpdatedAt(LocalDateTime.now());
        Driver updated = driverRepository.save(driver);
        Optional<Vehicle> vehicleOpt = vehicleRepository.findByDriverId(updated.getId());
        return mapToDriverResponse(updated, vehicleOpt.orElse(null));
    }

    @Override
    @Transactional
    public DriverResponse updateAvailability(String id, UpdateAvailabilityRequest request) {
        log.info("Updating availability for driver id: {} to {}", id, request.getAvailabilityStatus());
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));

        // Business rule: Driver cannot set status to AVAILABLE without a registered vehicle
        if (request.getAvailabilityStatus() == AvailabilityStatus.AVAILABLE) {
            boolean hasVehicle = vehicleRepository.existsByDriverId(driver.getId());
            if (!hasVehicle) {
                throw new BadRequestException("Driver must have a registered vehicle before setting availability to AVAILABLE");
            }
        }

        driver.setAvailabilityStatus(request.getAvailabilityStatus());
        driver.setUpdatedAt(LocalDateTime.now());
        Driver updated = driverRepository.save(driver);

        Optional<Vehicle> vehicleOpt = vehicleRepository.findByDriverId(updated.getId());
        return mapToDriverResponse(updated, vehicleOpt.orElse(null));
    }

    @Override
    @Transactional
    public DriverResponse updateLocation(String id, UpdateLocationRequest request) {
        log.info("Updating simulated location for driver id: {} to ({}, {})",
                id, request.getLatitude(), request.getLongitude());

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));

        Location newLocation = Location.builder()
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .updatedAt(LocalDateTime.now())
                .build();

        driver.setCurrentLocation(newLocation);
        driver.setUpdatedAt(LocalDateTime.now());
        Driver updated = driverRepository.save(driver);

        Optional<Vehicle> vehicleOpt = vehicleRepository.findByDriverId(updated.getId());
        return mapToDriverResponse(updated, vehicleOpt.orElse(null));
    }

    @Override
    @Transactional
    public DriverResponse updateServiceArea(String id, UpdateServiceAreaRequest request) {
        log.info("Updating service area for driver id: {} to '{}'", id, request.getServiceArea());
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));

        driver.setServiceArea(request.getServiceArea().trim());
        driver.setUpdatedAt(LocalDateTime.now());
        Driver updated = driverRepository.save(driver);

        Optional<Vehicle> vehicleOpt = vehicleRepository.findByDriverId(updated.getId());
        return mapToDriverResponse(updated, vehicleOpt.orElse(null));
    }

    @Override
    public List<AvailableDriverResponse> getAvailableDrivers(
            String serviceArea,
            VehicleType vehicleType,
            Double latitude,
            Double longitude,
            Double radiusKm) {

        log.info("Searching available drivers with serviceArea='{}', vehicleType='{}', coords=({}, {}), radiusKm={}",
                serviceArea, vehicleType, latitude, longitude, radiusKm);

        // 1. Fetch available drivers filtered by status and optional serviceArea
        List<Driver> availableDrivers;
        if (serviceArea != null && !serviceArea.isBlank()) {
            availableDrivers = driverRepository.findByAvailabilityStatusAndServiceAreaIgnoreCase(
                    AvailabilityStatus.AVAILABLE, serviceArea.trim());
        } else {
            availableDrivers = driverRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        }

        if (availableDrivers.isEmpty()) {
            return List.of();
        }

        // 2. Fetch associated vehicles for these drivers
        List<String> driverIds = availableDrivers.stream()
                .map(Driver::getId)
                .collect(Collectors.toList());

        Map<String, Vehicle> vehicleByDriverId = vehicleRepository.findByDriverIdIn(driverIds).stream()
                .collect(Collectors.toMap(Vehicle::getDriverId, v -> v, (v1, v2) -> v1));

        // 3. Filter drivers who have vehicles and match vehicleType if requested
        List<AvailableDriverResponse> results = new ArrayList<>();
        double effectiveRadius = (radiusKm != null && radiusKm > 0) ? radiusKm : 25.0;

        for (Driver driver : availableDrivers) {
            Vehicle vehicle = vehicleByDriverId.get(driver.getId());
            if (vehicle == null) {
                // Cannot dispatch a driver without vehicle
                continue;
            }

            if (vehicleType != null && vehicle.getVehicleType() != vehicleType) {
                continue;
            }

            Double distance = null;
            if (latitude != null && longitude != null && driver.getCurrentLocation() != null
                    && driver.getCurrentLocation().getLatitude() != null
                    && driver.getCurrentLocation().getLongitude() != null) {

                distance = GeoUtils.calculateDistanceKm(
                        latitude, longitude,
                        driver.getCurrentLocation().getLatitude(),
                        driver.getCurrentLocation().getLongitude());

                // Exclude if driver is beyond radius
                if (distance > effectiveRadius) {
                    continue;
                }
            }

            LocationResponse locResponse = null;
            if (driver.getCurrentLocation() != null) {
                locResponse = LocationResponse.builder()
                        .latitude(driver.getCurrentLocation().getLatitude())
                        .longitude(driver.getCurrentLocation().getLongitude())
                        .address(driver.getCurrentLocation().getAddress())
                        .updatedAt(driver.getCurrentLocation().getUpdatedAt())
                        .build();
            }

            AvailableDriverResponse item = AvailableDriverResponse.builder()
                    .driverId(driver.getId())
                    .accountId(driver.getAccountId())
                    .licenseNumber(driver.getLicenseNumber())
                    .serviceArea(driver.getServiceArea())
                    .currentLocation(locResponse)
                    .vehicleId(vehicle.getId())
                    .vehicleNumber(vehicle.getVehicleNumber())
                    .vehicleType(vehicle.getVehicleType())
                    .vehicleModel(vehicle.getModel())
                    .distanceKm(distance)
                    .build();

            results.add(item);
        }

        // Sort by distance if coordinates were provided
        if (latitude != null && longitude != null) {
            results.sort(Comparator.comparing(
                    AvailableDriverResponse::getDistanceKm,
                    Comparator.nullsLast(Double::compareTo)));
        }

        log.info("Found {} eligible available drivers", results.size());
        return results;
    }

    @Override
    public List<DriverResponse> getAllDrivers() {
        List<Driver> drivers = driverRepository.findAll();
        List<String> driverIds = drivers.stream().map(Driver::getId).collect(Collectors.toList());
        Map<String, Vehicle> vehicleMap = vehicleRepository.findByDriverIdIn(driverIds).stream()
                .collect(Collectors.toMap(Vehicle::getDriverId, v -> v, (v1, v2) -> v1));

        return drivers.stream()
                .map(d -> mapToDriverResponse(d, vehicleMap.get(d.getId())))
                .collect(Collectors.toList());
    }

    private DriverResponse mapToDriverResponse(Driver driver, Vehicle vehicle) {
        LocationResponse locResponse = null;
        if (driver.getCurrentLocation() != null) {
            locResponse = LocationResponse.builder()
                    .latitude(driver.getCurrentLocation().getLatitude())
                    .longitude(driver.getCurrentLocation().getLongitude())
                    .address(driver.getCurrentLocation().getAddress())
                    .updatedAt(driver.getCurrentLocation().getUpdatedAt())
                    .build();
        }

        VehicleResponse vehicleResponse = null;
        if (vehicle != null) {
            vehicleResponse = VehicleResponse.builder()
                    .id(vehicle.getId())
                    .driverId(vehicle.getDriverId())
                    .vehicleNumber(vehicle.getVehicleNumber())
                    .vehicleType(vehicle.getVehicleType())
                    .model(vehicle.getModel())
                    .createdAt(vehicle.getCreatedAt())
                    .updatedAt(vehicle.getUpdatedAt())
                    .build();
        }

        return DriverResponse.builder()
                .id(driver.getId())
                .accountId(driver.getAccountId())
                .licenseNumber(driver.getLicenseNumber())
                .availabilityStatus(driver.getAvailabilityStatus())
                .serviceArea(driver.getServiceArea())
                .currentLocation(locResponse)
                .vehicle(vehicleResponse)
                .createdAt(driver.getCreatedAt())
                .updatedAt(driver.getUpdatedAt())
                .build();
    }
}

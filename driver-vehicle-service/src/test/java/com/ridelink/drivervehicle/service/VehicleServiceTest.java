package com.ridelink.drivervehicle.service;

import com.ridelink.drivervehicle.dto.request.CreateVehicleRequest;
import com.ridelink.drivervehicle.dto.request.UpdateVehicleRequest;
import com.ridelink.drivervehicle.dto.response.VehicleResponse;
import com.ridelink.drivervehicle.exception.BadRequestException;
import com.ridelink.drivervehicle.exception.DuplicateResourceException;
import com.ridelink.drivervehicle.exception.ResourceNotFoundException;
import com.ridelink.drivervehicle.model.Vehicle;
import com.ridelink.drivervehicle.model.VehicleType;
import com.ridelink.drivervehicle.repository.DriverRepository;
import com.ridelink.drivervehicle.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        sampleVehicle = Vehicle.builder()
                .id("vehicle-1")
                .driverId("driver-1")
                .vehicleNumber("WP-CAB-1234")
                .vehicleType(VehicleType.CAR)
                .model("Toyota Prius 2020")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully register vehicle for driver")
    void createVehicle_Success() {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .driverId("driver-1")
                .vehicleNumber("wp-cab-1234")
                .vehicleType(VehicleType.CAR)
                .model("Toyota Prius 2020")
                .build();

        when(driverRepository.existsById("driver-1")).thenReturn(true);
        when(vehicleRepository.existsByVehicleNumber("wp-cab-1234")).thenReturn(false);
        when(vehicleRepository.existsByDriverId("driver-1")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle v = invocation.getArgument(0);
            v.setId("vehicle-1");
            return v;
        });

        VehicleResponse response = vehicleService.createVehicle(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("vehicle-1");
        assertThat(response.getVehicleNumber()).isEqualTo("WP-CAB-1234");
        assertThat(response.getVehicleType()).isEqualTo(VehicleType.CAR);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when driver not found")
    void createVehicle_DriverNotFound_ThrowsException() {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .driverId("nonexistent-driver")
                .vehicleNumber("WP-CAB-1234")
                .vehicleType(VehicleType.CAR)
                .model("Toyota Prius")
                .build();

        when(driverRepository.existsById("nonexistent-driver")).thenReturn(false);

        assertThatThrownBy(() -> vehicleService.createVehicle(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Driver not found");
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when vehicle number already exists")
    void createVehicle_DuplicateNumber_ThrowsException() {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .driverId("driver-1")
                .vehicleNumber("WP-CAB-1234")
                .vehicleType(VehicleType.CAR)
                .model("Toyota Prius")
                .build();

        when(driverRepository.existsById("driver-1")).thenReturn(true);
        when(vehicleRepository.existsByVehicleNumber("WP-CAB-1234")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.createVehicle(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should throw BadRequestException when driver already has a vehicle")
    void createVehicle_DriverAlreadyHasVehicle_ThrowsException() {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .driverId("driver-1")
                .vehicleNumber("WP-CAB-5555")
                .vehicleType(VehicleType.CAR)
                .model("Toyota Prius")
                .build();

        when(driverRepository.existsById("driver-1")).thenReturn(true);
        when(vehicleRepository.existsByVehicleNumber("WP-CAB-5555")).thenReturn(false);
        when(vehicleRepository.existsByDriverId("driver-1")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.createVehicle(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already has a registered vehicle");
    }

    @Test
    @DisplayName("Should successfully retrieve vehicle by ID")
    void getVehicleById_Success() {
        when(vehicleRepository.findById("vehicle-1")).thenReturn(Optional.of(sampleVehicle));

        VehicleResponse response = vehicleService.getVehicleById("vehicle-1");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("vehicle-1");
        assertThat(response.getVehicleNumber()).isEqualTo("WP-CAB-1234");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when vehicle not found by ID")
    void getVehicleById_NotFound_ThrowsException() {
        when(vehicleRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getVehicleById("invalid-id"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vehicle not found");
    }

    @Test
    @DisplayName("Should update vehicle details")
    void updateVehicle_Success() {
        UpdateVehicleRequest request = UpdateVehicleRequest.builder()
                .model("Toyota Prius 2022")
                .vehicleType(VehicleType.CAR)
                .build();

        when(vehicleRepository.findById("vehicle-1")).thenReturn(Optional.of(sampleVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(sampleVehicle);

        VehicleResponse response = vehicleService.updateVehicle("vehicle-1", request);

        assertThat(response).isNotNull();
        assertThat(sampleVehicle.getModel()).isEqualTo("Toyota Prius 2022");
    }

    @Test
    @DisplayName("Should delete vehicle by ID")
    void deleteVehicle_Success() {
        when(vehicleRepository.findById("vehicle-1")).thenReturn(Optional.of(sampleVehicle));

        vehicleService.deleteVehicle("vehicle-1");

        verify(vehicleRepository).delete(sampleVehicle);
    }
}

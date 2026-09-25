package com.ridelink.drivervehicle.service;

import com.ridelink.drivervehicle.dto.request.CreateDriverRequest;
import com.ridelink.drivervehicle.dto.request.UpdateAvailabilityRequest;
import com.ridelink.drivervehicle.dto.request.UpdateDriverRequest;
import com.ridelink.drivervehicle.dto.request.UpdateLocationRequest;
import com.ridelink.drivervehicle.dto.request.UpdateServiceAreaRequest;
import com.ridelink.drivervehicle.dto.response.AvailableDriverResponse;
import com.ridelink.drivervehicle.dto.response.DriverResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private DriverServiceImpl driverService;

    private Driver sampleDriver;
    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        sampleDriver = Driver.builder()
                .id("driver-1")
                .accountId("acc-101")
                .licenseNumber("B1234567")
                .serviceArea("Colombo")
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .currentLocation(Location.builder()
                        .latitude(6.9271)
                        .longitude(79.8612)
                        .address("Colombo Fort")
                        .updatedAt(LocalDateTime.now())
                        .build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

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
    @DisplayName("Should successfully create a driver operational profile")
    void createDriver_Success() {
        CreateDriverRequest request = CreateDriverRequest.builder()
                .accountId("acc-101")
                .licenseNumber("b1234567")
                .serviceArea("Colombo")
                .initialLatitude(6.9271)
                .initialLongitude(79.8612)
                .initialAddress("Colombo Fort")
                .build();

        when(driverRepository.existsByAccountId("acc-101")).thenReturn(false);
        when(driverRepository.existsByLicenseNumber("B1234567")).thenReturn(false);
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> {
            Driver d = invocation.getArgument(0);
            d.setId("driver-1");
            return d;
        });

        DriverResponse response = driverService.createDriver(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("driver-1");
        assertThat(response.getAccountId()).isEqualTo("acc-101");
        assertThat(response.getLicenseNumber()).isEqualTo("B1234567");
        assertThat(response.getServiceArea()).isEqualTo("Colombo");
        assertThat(response.getCurrentLocation()).isNotNull();
        assertThat(response.getCurrentLocation().getLatitude()).isEqualTo(6.9271);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating driver with existing accountId")
    void createDriver_DuplicateAccountId_ThrowsException() {
        CreateDriverRequest request = CreateDriverRequest.builder()
                .accountId("acc-101")
                .licenseNumber("B1234567")
                .serviceArea("Colombo")
                .build();

        when(driverRepository.existsByAccountId("acc-101")).thenReturn(true);

        assertThatThrownBy(() -> driverService.createDriver(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists for accountId");
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating driver with existing licenseNumber")
    void createDriver_DuplicateLicense_ThrowsException() {
        CreateDriverRequest request = CreateDriverRequest.builder()
                .accountId("acc-102")
                .licenseNumber("B1234567")
                .serviceArea("Colombo")
                .build();

        when(driverRepository.existsByAccountId("acc-102")).thenReturn(false);
        when(driverRepository.existsByLicenseNumber("B1234567")).thenReturn(true);

        assertThatThrownBy(() -> driverService.createDriver(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("license number");
    }

    @Test
    @DisplayName("Should successfully retrieve driver by ID")
    void getDriverById_Success() {
        when(driverRepository.findById("driver-1")).thenReturn(Optional.of(sampleDriver));
        when(vehicleRepository.findByDriverId("driver-1")).thenReturn(Optional.of(sampleVehicle));

        DriverResponse response = driverService.getDriverById("driver-1");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("driver-1");
        assertThat(response.getVehicle()).isNotNull();
        assertThat(response.getVehicle().getVehicleNumber()).isEqualTo("WP-CAB-1234");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when driver not found by ID")
    void getDriverById_NotFound_ThrowsException() {
        when(driverRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.getDriverById("invalid-id"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Driver not found");
    }

    @Test
    @DisplayName("Should update general driver profile")
    void updateDriver_Success() {
        UpdateDriverRequest request = UpdateDriverRequest.builder()
                .licenseNumber("B7654321")
                .serviceArea("Kandy")
                .build();

        when(driverRepository.findById("driver-1")).thenReturn(Optional.of(sampleDriver));
        when(driverRepository.existsByLicenseNumber("B7654321")).thenReturn(false);
        when(driverRepository.save(any(Driver.class))).thenReturn(sampleDriver);
        when(vehicleRepository.findByDriverId("driver-1")).thenReturn(Optional.of(sampleVehicle));

        DriverResponse response = driverService.updateDriver("driver-1", request);

        assertThat(response).isNotNull();
        assertThat(sampleDriver.getLicenseNumber()).isEqualTo("B7654321");
        assertThat(sampleDriver.getServiceArea()).isEqualTo("Kandy");
    }

    @Test
    @DisplayName("Should successfully update driver availability status")
    void updateAvailability_Success() {
        UpdateAvailabilityRequest request = UpdateAvailabilityRequest.builder()
                .availabilityStatus(AvailabilityStatus.UNAVAILABLE)
                .build();

        when(driverRepository.findById("driver-1")).thenReturn(Optional.of(sampleDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(sampleDriver);
        when(vehicleRepository.findByDriverId("driver-1")).thenReturn(Optional.of(sampleVehicle));

        DriverResponse response = driverService.updateAvailability("driver-1", request);

        assertThat(response).isNotNull();
        assertThat(sampleDriver.getAvailabilityStatus()).isEqualTo(AvailabilityStatus.UNAVAILABLE);
    }

    @Test
    @DisplayName("Should throw BadRequestException when setting status to AVAILABLE without registered vehicle")
    void updateAvailability_NoVehicle_ThrowsException() {
        UpdateAvailabilityRequest request = UpdateAvailabilityRequest.builder()
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .build();

        when(driverRepository.findById("driver-1")).thenReturn(Optional.of(sampleDriver));
        when(vehicleRepository.existsByDriverId("driver-1")).thenReturn(false);

        assertThatThrownBy(() -> driverService.updateAvailability("driver-1", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must have a registered vehicle");
    }

    @Test
    @DisplayName("Should successfully update driver simulated location")
    void updateLocation_Success() {
        UpdateLocationRequest request = UpdateLocationRequest.builder()
                .latitude(6.9319)
                .longitude(79.8437)
                .address("Colombo Pettah")
                .build();

        when(driverRepository.findById("driver-1")).thenReturn(Optional.of(sampleDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(sampleDriver);
        when(vehicleRepository.findByDriverId("driver-1")).thenReturn(Optional.of(sampleVehicle));

        DriverResponse response = driverService.updateLocation("driver-1", request);

        assertThat(response).isNotNull();
        assertThat(sampleDriver.getCurrentLocation().getLatitude()).isEqualTo(6.9319);
        assertThat(sampleDriver.getCurrentLocation().getAddress()).isEqualTo("Colombo Pettah");
    }

    @Test
    @DisplayName("Should successfully update driver service area")
    void updateServiceArea_Success() {
        UpdateServiceAreaRequest request = UpdateServiceAreaRequest.builder()
                .serviceArea("Galle")
                .build();

        when(driverRepository.findById("driver-1")).thenReturn(Optional.of(sampleDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(sampleDriver);
        when(vehicleRepository.findByDriverId("driver-1")).thenReturn(Optional.of(sampleVehicle));

        DriverResponse response = driverService.updateServiceArea("driver-1", request);

        assertThat(response).isNotNull();
        assertThat(sampleDriver.getServiceArea()).isEqualTo("Galle");
    }

    @Test
    @DisplayName("Should retrieve available drivers filtered by serviceArea and vehicleType")
    void getAvailableDrivers_Filter_Success() {
        when(driverRepository.findByAvailabilityStatusAndServiceAreaIgnoreCase(
                AvailabilityStatus.AVAILABLE, "Colombo"))
                .thenReturn(List.of(sampleDriver));
        when(vehicleRepository.findByDriverIdIn(anyList()))
                .thenReturn(List.of(sampleVehicle));

        List<AvailableDriverResponse> available = driverService.getAvailableDrivers(
                "Colombo", VehicleType.CAR, null, null, null);

        assertThat(available).hasSize(1);
        assertThat(available.get(0).getDriverId()).isEqualTo("driver-1");
        assertThat(available.get(0).getVehicleNumber()).isEqualTo("WP-CAB-1234");
        assertThat(available.get(0).getVehicleType()).isEqualTo(VehicleType.CAR);
    }

    @Test
    @DisplayName("Should calculate proximity and sort available drivers by distance")
    void getAvailableDrivers_Proximity_Success() {
        Driver farDriver = Driver.builder()
                .id("driver-2")
                .accountId("acc-102")
                .licenseNumber("B2222222")
                .serviceArea("Colombo")
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .currentLocation(Location.builder().latitude(6.8500).longitude(79.8600).build())
                .build();

        Vehicle farVehicle = Vehicle.builder()
                .id("vehicle-2")
                .driverId("driver-2")
                .vehicleNumber("WP-CAR-9999")
                .vehicleType(VehicleType.CAR)
                .model("Suzuki Alto")
                .build();

        when(driverRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE))
                .thenReturn(List.of(farDriver, sampleDriver));
        when(vehicleRepository.findByDriverIdIn(anyList()))
                .thenReturn(List.of(farVehicle, sampleVehicle));

        // Pickup point at 6.9270, 79.8610 (very close to sampleDriver at 6.9271, 79.8612)
        List<AvailableDriverResponse> available = driverService.getAvailableDrivers(
                null, null, 6.9270, 79.8610, 20.0);

        assertThat(available).hasSize(2);
        // sampleDriver should be first because it is closest!
        assertThat(available.get(0).getDriverId()).isEqualTo("driver-1");
        assertThat(available.get(0).getDistanceKm()).isLessThan(available.get(1).getDistanceKm());
    }
}

package com.ridelink.drivervehicle.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.drivervehicle.config.JwtUtil;
import com.ridelink.drivervehicle.dto.request.CreateDriverRequest;
import com.ridelink.drivervehicle.dto.request.UpdateAvailabilityRequest;
import com.ridelink.drivervehicle.dto.request.UpdateLocationRequest;
import com.ridelink.drivervehicle.dto.response.AvailableDriverResponse;
import com.ridelink.drivervehicle.dto.response.DriverResponse;
import com.ridelink.drivervehicle.exception.ResourceNotFoundException;
import com.ridelink.drivervehicle.model.AvailabilityStatus;
import com.ridelink.drivervehicle.model.VehicleType;
import com.ridelink.drivervehicle.service.DriverService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DriverController.class)
@AutoConfigureMockMvc(addFilters = false)
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DriverService driverService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("POST /api/drivers - Success 201 Created")
    void createDriver_Success() throws Exception {
        CreateDriverRequest request = CreateDriverRequest.builder()
                .accountId("acc-123")
                .licenseNumber("B1234567")
                .serviceArea("Colombo")
                .build();

        DriverResponse response = DriverResponse.builder()
                .id("driver-1")
                .accountId("acc-123")
                .licenseNumber("B1234567")
                .serviceArea("Colombo")
                .availabilityStatus(AvailabilityStatus.OFFLINE)
                .build();

        when(driverService.createDriver(any(CreateDriverRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("driver-1"))
                .andExpect(jsonPath("$.accountId").value("acc-123"))
                .andExpect(jsonPath("$.licenseNumber").value("B1234567"));
    }

    @Test
    @DisplayName("POST /api/drivers - 400 Bad Request on Missing Required Fields")
    void createDriver_ValidationError() throws Exception {
        CreateDriverRequest invalidRequest = CreateDriverRequest.builder()
                .accountId("") // Blank
                .licenseNumber("") // Blank
                .build();

        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    @DisplayName("GET /api/drivers/{id} - Success 200 OK")
    void getDriverById_Success() throws Exception {
        DriverResponse response = DriverResponse.builder()
                .id("driver-1")
                .accountId("acc-123")
                .licenseNumber("B1234567")
                .serviceArea("Colombo")
                .build();

        when(driverService.getDriverById("driver-1")).thenReturn(response);

        mockMvc.perform(get("/api/drivers/driver-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("driver-1"))
                .andExpect(jsonPath("$.serviceArea").value("Colombo"));
    }

    @Test
    @DisplayName("GET /api/drivers/{id} - 404 Not Found")
    void getDriverById_NotFound() throws Exception {
        when(driverService.getDriverById("driver-999"))
                .thenThrow(new ResourceNotFoundException("Driver not found with id: driver-999"));

        mockMvc.perform(get("/api/drivers/driver-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Driver not found with id: driver-999"));
    }

    @Test
    @DisplayName("PUT /api/drivers/{id}/availability - Success 200 OK")
    void updateAvailability_Success() throws Exception {
        UpdateAvailabilityRequest request = UpdateAvailabilityRequest.builder()
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .build();

        DriverResponse response = DriverResponse.builder()
                .id("driver-1")
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .build();

        when(driverService.updateAvailability(eq("driver-1"), any(UpdateAvailabilityRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/drivers/driver-1/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availabilityStatus").value("AVAILABLE"));
    }

    @Test
    @DisplayName("PUT /api/drivers/{id}/location - Success 200 OK")
    void updateLocation_Success() throws Exception {
        UpdateLocationRequest request = UpdateLocationRequest.builder()
                .latitude(6.9271)
                .longitude(79.8612)
                .address("Colombo Fort")
                .build();

        DriverResponse response = DriverResponse.builder()
                .id("driver-1")
                .build();

        when(driverService.updateLocation(eq("driver-1"), any(UpdateLocationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/drivers/driver-1/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("driver-1"));
    }

    @Test
    @DisplayName("GET /api/drivers/available - Success 200 OK")
    void getAvailableDrivers_Success() throws Exception {
        AvailableDriverResponse availableDriver = AvailableDriverResponse.builder()
                .driverId("driver-1")
                .accountId("acc-123")
                .licenseNumber("B1234567")
                .serviceArea("Colombo")
                .vehicleNumber("WP-CAB-1234")
                .vehicleType(VehicleType.CAR)
                .build();

        when(driverService.getAvailableDrivers(any(), any(), any(), any(), any()))
                .thenReturn(List.of(availableDriver));

        mockMvc.perform(get("/api/drivers/available?serviceArea=Colombo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].driverId").value("driver-1"))
                .andExpect(jsonPath("$[0].vehicleNumber").value("WP-CAB-1234"));
    }
}

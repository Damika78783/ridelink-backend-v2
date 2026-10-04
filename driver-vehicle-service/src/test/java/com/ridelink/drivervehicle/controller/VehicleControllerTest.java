package com.ridelink.drivervehicle.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.drivervehicle.config.JwtUtil;
import com.ridelink.drivervehicle.dto.request.CreateVehicleRequest;
import com.ridelink.drivervehicle.dto.request.UpdateVehicleRequest;
import com.ridelink.drivervehicle.dto.response.VehicleResponse;
import com.ridelink.drivervehicle.exception.ResourceNotFoundException;
import com.ridelink.drivervehicle.model.VehicleType;
import com.ridelink.drivervehicle.service.VehicleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleService vehicleService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("POST /api/vehicles - Success 201 Created")
    void createVehicle_Success() throws Exception {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .driverId("driver-1")
                .vehicleNumber("WP-CAB-1234")
                .vehicleType(VehicleType.CAR)
                .model("Toyota Prius 2020")
                .build();

        VehicleResponse response = VehicleResponse.builder()
                .id("vehicle-1")
                .driverId("driver-1")
                .vehicleNumber("WP-CAB-1234")
                .vehicleType(VehicleType.CAR)
                .model("Toyota Prius 2020")
                .build();

        when(vehicleService.createVehicle(any(CreateVehicleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("vehicle-1"))
                .andExpect(jsonPath("$.vehicleNumber").value("WP-CAB-1234"))
                .andExpect(jsonPath("$.vehicleType").value("CAR"));
    }

    @Test
    @DisplayName("GET /api/vehicles/{id} - Success 200 OK")
    void getVehicleById_Success() throws Exception {
        VehicleResponse response = VehicleResponse.builder()
                .id("vehicle-1")
                .driverId("driver-1")
                .vehicleNumber("WP-CAB-1234")
                .vehicleType(VehicleType.CAR)
                .model("Toyota Prius 2020")
                .build();

        when(vehicleService.getVehicleById("vehicle-1")).thenReturn(response);

        mockMvc.perform(get("/api/vehicles/vehicle-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("vehicle-1"))
                .andExpect(jsonPath("$.vehicleNumber").value("WP-CAB-1234"));
    }

    @Test
    @DisplayName("GET /api/vehicles/{id} - 404 Not Found")
    void getVehicleById_NotFound() throws Exception {
        when(vehicleService.getVehicleById("vehicle-999"))
                .thenThrow(new ResourceNotFoundException("Vehicle not found with id: vehicle-999"));

        mockMvc.perform(get("/api/vehicles/vehicle-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/vehicles/driver/{driverId} - Success 200 OK")
    void getVehicleByDriverId_Success() throws Exception {
        VehicleResponse response = VehicleResponse.builder()
                .id("vehicle-1")
                .driverId("driver-1")
                .vehicleNumber("WP-CAB-1234")
                .build();

        when(vehicleService.getVehicleByDriverId("driver-1")).thenReturn(response);

        mockMvc.perform(get("/api/vehicles/driver/driver-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverId").value("driver-1"));
    }

    @Test
    @DisplayName("PUT /api/vehicles/{id} - Success 200 OK")
    void updateVehicle_Success() throws Exception {
        UpdateVehicleRequest request = UpdateVehicleRequest.builder()
                .model("Toyota Aqua 2021")
                .build();

        VehicleResponse response = VehicleResponse.builder()
                .id("vehicle-1")
                .model("Toyota Aqua 2021")
                .build();

        when(vehicleService.updateVehicle(eq("vehicle-1"), any(UpdateVehicleRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/vehicles/vehicle-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Toyota Aqua 2021"));
    }

    @Test
    @DisplayName("DELETE /api/vehicles/{id} - Success 204 No Content")
    void deleteVehicle_Success() throws Exception {
        doNothing().when(vehicleService).deleteVehicle("vehicle-1");

        mockMvc.perform(delete("/api/vehicles/vehicle-1"))
                .andExpect(status().isNoContent());
    }
}

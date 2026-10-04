package com.ridelink.drivervehicle.service;

import com.ridelink.drivervehicle.dto.request.CreateVehicleRequest;
import com.ridelink.drivervehicle.dto.request.UpdateVehicleRequest;
import com.ridelink.drivervehicle.dto.response.VehicleResponse;

import java.util.List;

public interface VehicleService {

    VehicleResponse createVehicle(CreateVehicleRequest request);

    VehicleResponse getVehicleById(String id);

    VehicleResponse getVehicleByDriverId(String driverId);

    VehicleResponse updateVehicle(String id, UpdateVehicleRequest request);

    void deleteVehicle(String id);

    List<VehicleResponse> getAllVehicles();
}

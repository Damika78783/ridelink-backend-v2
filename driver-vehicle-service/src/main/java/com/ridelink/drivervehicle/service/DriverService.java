package com.ridelink.drivervehicle.service;

import com.ridelink.drivervehicle.dto.request.CreateDriverRequest;
import com.ridelink.drivervehicle.dto.request.UpdateAvailabilityRequest;
import com.ridelink.drivervehicle.dto.request.UpdateDriverRequest;
import com.ridelink.drivervehicle.dto.request.UpdateLocationRequest;
import com.ridelink.drivervehicle.dto.request.UpdateServiceAreaRequest;
import com.ridelink.drivervehicle.dto.response.AvailableDriverResponse;
import com.ridelink.drivervehicle.dto.response.DriverResponse;
import com.ridelink.drivervehicle.model.VehicleType;

import java.util.List;

public interface DriverService {

    DriverResponse createDriver(CreateDriverRequest request);

    DriverResponse getDriverById(String id);

    DriverResponse getDriverByAccountId(String accountId);

    DriverResponse updateDriver(String id, UpdateDriverRequest request);

    DriverResponse updateAvailability(String id, UpdateAvailabilityRequest request);

    DriverResponse updateLocation(String id, UpdateLocationRequest request);

    DriverResponse updateServiceArea(String id, UpdateServiceAreaRequest request);

    List<AvailableDriverResponse> getAvailableDrivers(
            String serviceArea,
            VehicleType vehicleType,
            Double latitude,
            Double longitude,
            Double radiusKm);

    List<DriverResponse> getAllDrivers();
}

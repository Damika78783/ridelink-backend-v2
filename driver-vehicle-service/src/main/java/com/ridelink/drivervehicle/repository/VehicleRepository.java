package com.ridelink.drivervehicle.repository;

import com.ridelink.drivervehicle.model.Vehicle;
import com.ridelink.drivervehicle.model.VehicleType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {

    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);

    Optional<Vehicle> findByDriverId(String driverId);

    boolean existsByVehicleNumber(String vehicleNumber);

    boolean existsByDriverId(String driverId);

    List<Vehicle> findByDriverIdIn(List<String> driverIds);

    List<Vehicle> findByVehicleType(VehicleType vehicleType);
}

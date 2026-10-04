package com.ridelink.drivervehicle.repository;

import com.ridelink.drivervehicle.model.AvailabilityStatus;
import com.ridelink.drivervehicle.model.Driver;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends MongoRepository<Driver, String> {

    Optional<Driver> findByAccountId(String accountId);

    Optional<Driver> findByLicenseNumber(String licenseNumber);

    boolean existsByAccountId(String accountId);

    boolean existsByLicenseNumber(String licenseNumber);

    List<Driver> findByAvailabilityStatus(AvailabilityStatus availabilityStatus);

    List<Driver> findByAvailabilityStatusAndServiceAreaIgnoreCase(
            AvailabilityStatus availabilityStatus, String serviceArea);
}

package lk.sliit.it3130.ride_management_service.repository;

import lk.sliit.it3130.ride_management_service.model.Ride;
import lk.sliit.it3130.ride_management_service.model.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {

    List<Ride> findByPassengerEmail(String passengerEmail);

    List<Ride> findByDriverEmail(String driverEmail);

    List<Ride> findByStatus(RideStatus status);
}
package lk.sliit.it3130.fare_payment_service.repository;

import lk.sliit.it3130.fare_payment_service.model.Payment;
import lk.sliit.it3130.fare_payment_service.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRideId(Long rideId);

    List<Payment> findAllByRideId(Long rideId);

    List<Payment> findByPassengerId(Long passengerId);

    boolean existsByRideIdAndStatus(Long rideId, PaymentStatus status);

    Optional<Payment> findByTransactionId(String transactionId);
}

package lk.sliit.it3130.fare_payment_service.exception;

public class RideNotCompletedException extends RuntimeException {
    public RideNotCompletedException(String message) {
        super(message);
    }
}

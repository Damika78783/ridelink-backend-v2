package lk.sliit.it3130.fare_payment_service.exception;

public class InvalidFareException extends RuntimeException {
    public InvalidFareException(String message) {
        super(message);
    }
}

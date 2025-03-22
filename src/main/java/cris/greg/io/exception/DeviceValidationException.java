package cris.greg.io.exception;

import org.springframework.http.HttpStatus;

public class DeviceValidationException extends RuntimeException {
    private final HttpStatus status;

    public DeviceValidationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
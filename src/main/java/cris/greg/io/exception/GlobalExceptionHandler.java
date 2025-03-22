package cris.greg.io.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DeviceValidationException.class)
    public ResponseEntity<Object> handleDeviceValidationException(DeviceValidationException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", ex.getStatus().value());
        body.put("error", ex.getStatus().getReasonPhrase());
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now().toString());

        return new ResponseEntity<>(body, ex.getStatus());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("message", "Malformed JSON request. Check the request body and try again.");
        body.put("timestamp", LocalDateTime.now().toString());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
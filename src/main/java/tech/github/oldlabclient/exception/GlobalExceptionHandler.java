package tech.github.oldlabclient.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    record ApiError(Instant timestamp, String code, String message) {}

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> onNotFound(UserNotFoundException ex) {
        ApiError err = new ApiError(
            Instant.now(),
            "USER_NOT_FOUND",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> onProductNotFound(ProductNotFoundException ex) {
        ApiError err = new ApiError(
                Instant.now(),
                "PRODUCT_NOT_FOUND",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> onExists(UserAlreadyExistsException ex) {
        ApiError err = new ApiError(
                Instant.now(),
                "USER_ALREADY_EXISTS",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> onValidation(MethodArgumentNotValidException ex) {
        ApiError err = new ApiError(
            Instant.now(),
            "VALIDATION_ERROR",
            "invalid request data"
        );
        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> onBadCredentials(BadCredentialsException ex) {
        ApiError err = new ApiError(
            Instant.now(),
            "INVALID_CREDENTIALS",
            "invalid credentials"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> onGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        ApiError err = new ApiError(
            Instant.now(),
            "SERVER_ERROR",
            "unexpected error"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}

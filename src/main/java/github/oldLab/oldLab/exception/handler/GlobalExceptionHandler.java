package github.oldLab.oldLab.exception.handler;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import github.oldLab.oldLab.exception.UserNotFoundException;
import github.oldLab.oldLab.exception.InvalidTokenException;
import github.oldLab.oldLab.exception.InvalidPasswordException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    record ApiError(Instant timestamp, String code, String message, Object details) {}

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> onNotFound(UserNotFoundException ex) {
        ApiError err = new ApiError(
            Instant.now(),
            "USER_NOT_FOUND",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> onValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.toList());
        ApiError err = new ApiError(
            Instant.now(),
            "VALIDATION_ERROR",
            "invalid request data",
            errors
        );
        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiError> onInvalidToken(InvalidTokenException ex) {
        ApiError err = new ApiError(
            Instant.now(),
            "INVALID_TOKEN",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiError> onInvalidPassword(InvalidPasswordException ex) {
        ApiError err = new ApiError(
            Instant.now(),
            "INVALID_PASSWORD",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> onGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        ApiError err = new ApiError(
            Instant.now(),
            "SERVER_ERROR",
            "unexpected error",
            null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}


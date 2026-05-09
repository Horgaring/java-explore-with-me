package ru.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleBadRequestException(final BadRequestException e) {
        log.error("Bad request exception: {}", e.getMessage());
        ApiError apiError = new ApiError(
                e.getMessage(),
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST,
                List.of(e.getMessage())
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleConstraintViolationException(final ConstraintViolationException e) {
        log.error("Constraint violation exception: {}", e.getMessage());
        ApiError apiError = new ApiError(
                e.getMessage(),
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST,
                List.of(e.getMessage())
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.error("Method argument not valid: {}", e.getMessage());
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("Field: %s. Error: %s. Value: %s",
                        error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                .findFirst()
                .orElse(e.getMessage());
        ApiError apiError = new ApiError(
                message,
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST,
                List.of(e.getMessage())
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiError> handleNotFoundException(final NotFoundException e) {
        log.error("Not found exception: {}", e.getMessage());
        ApiError apiError = new ApiError(
                e.getMessage(),
                "The required object was not found.",
                HttpStatus.NOT_FOUND,
                List.of(e.getMessage())
        );
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiError> handleConflictException(final ConflictException e) {
        log.error("Conflict exception: {}", e.getMessage());
        ApiError apiError = new ApiError(
                e.getMessage(),
                "For the requested operation the conditions are not met.",
                HttpStatus.CONFLICT,
                List.of(e.getMessage())
        );
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }
}

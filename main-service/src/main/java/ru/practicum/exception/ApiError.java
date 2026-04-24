package ru.practicum.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ApiError {
    private List<String> errors;
    private String message;
    private String reason;
    private HttpStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    public ApiError(String message, String reason, HttpStatus status, List<String> errors) {
        this.message = message;
        this.reason = reason;
        this.status = status;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }
}
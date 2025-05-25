package ru.webrise.technicaltask.util.handlers;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DataErrorResponse {

    private String message;

    private Throwable cause;

    private LocalDateTime timestamp;

    public DataErrorResponse(String message, LocalDateTime timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }
}
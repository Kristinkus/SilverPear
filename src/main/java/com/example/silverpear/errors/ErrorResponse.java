package com.example.silverpear.errors;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private String error;
    private LocalDateTime timestamp;
    private int status;
}
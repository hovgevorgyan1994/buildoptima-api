package com.vecondev.buildoptima.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ApiError {

    private HttpStatus status;
    private LocalDateTime timestamp;
    private String message;
}
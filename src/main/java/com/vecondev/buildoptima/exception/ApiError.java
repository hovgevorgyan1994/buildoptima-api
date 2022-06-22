package com.vecondev.buildoptima.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(name = "Error response")
public class ApiError {

  @Schema(description = "Http response status", example = "BAD_REQUEST")
  private HttpStatus status;

  @Schema(description = "Timestamp showing when the error occurred")
  private LocalDateTime timestamp;

  @Schema(
      description = "Error message",
      example = "There is an user registered with such an email!")
  private String message;
}

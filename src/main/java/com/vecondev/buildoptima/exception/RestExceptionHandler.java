package com.vecondev.buildoptima.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      @NotNull MethodArgumentNotValidException ex,
      @NotNull HttpHeaders headers,
      @NotNull HttpStatus status,
      @NotNull WebRequest request) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", status);

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().stream()
        .forEach(fieldError -> errors.put(fieldError.getField(), fieldError.getDefaultMessage()));
    body.put("errors", errors);

    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler({BaseException.class})
  public ResponseEntity<ApiError> handleIllegalArgument(BaseException ex) {
    ApiError apiError =
        new ApiError(
            ex.getErrorCode().getHttpStatus(),
            ex.getErrorCode(),
            LocalDateTime.now(),
            ex.getErrorCode().getMessage());

    return new ResponseEntity<>(apiError, apiError.getStatus());
  }






}

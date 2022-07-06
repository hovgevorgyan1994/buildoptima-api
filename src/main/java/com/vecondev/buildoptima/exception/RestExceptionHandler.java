package com.vecondev.buildoptima.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.AlreadyBuiltException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.UnexpectedTypeException;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;


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

  @ExceptionHandler({IllegalArgumentException.class})
  public ResponseEntity<ApiError> handleIllegalArgument(Exception ex) {
    return handleAsApiError(ex, BAD_REQUEST);
  }

  @ExceptionHandler({AlreadyBuiltException.class})
  public ResponseEntity<ApiError> handleAlreadyBuilt(AlreadyBuiltException ex) {
    return handleAsApiError(ex, CONFLICT);
  }

  @ExceptionHandler({AccessDeniedException.class})
  public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex){
    return handleAsApiError(ex, FORBIDDEN);
  }

  @ExceptionHandler({ResourceNotFoundException.class})
  public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException ex) {
    return handleAsApiError(ex, ex.getStatus());
  }

  @ExceptionHandler({AuthenticationException.class})
  public ResponseEntity<ApiError> handle(AuthenticationException ex) {
    ApiError error =
            new ApiError(
                    ex.getErrorCode().getHttpStatus(), LocalDateTime.now(), ex.getErrorCode().getMessage());
    return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(error);
  }

  @ExceptionHandler({UnexpectedTypeException.class})
  public ResponseEntity<ApiError> handleUnexpectedType(UnexpectedTypeException ex) {
    return handleAsApiError(ex, INTERNAL_SERVER_ERROR);
  }

  /**
   * handles exceptions and constructs responses for them
   *
   * @param status
   * @param <T> exception
   */
  public <T extends Exception> ResponseEntity<ApiError> handleAsApiError(T ex, HttpStatus status) {
    ApiError error = new ApiError(status, LocalDateTime.now(), ex.getMessage());
    return new ResponseEntity<>(error, status);
  }

  @ExceptionHandler({WrongFieldException.class})
  public ResponseEntity<ApiError> handle(WrongFieldException ex) {
    ApiError error =
        new ApiError(
            BAD_REQUEST, LocalDateTime.now(), ex.getMessage());
    return ResponseEntity.status(BAD_REQUEST).body(error);
  }
}

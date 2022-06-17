package com.vecondev.buildoptima.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status);

        List<String> errors = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .toList();
        body.put("errors", errors);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ApiError> handleAll(Exception ex){
        ApiError error =  new ApiError(HttpStatus.BAD_REQUEST, LocalDateTime.now(), ex.getLocalizedMessage());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler({AuthException.class})
    public ResponseEntity<ApiError> handle(AuthException e){
        ApiError error = new ApiError(e.getErrorCode().getHttpStatus(),LocalDateTime.now(),e.getErrorCode().getMessage());
        return ResponseEntity.badRequest().body(error);
    }


}
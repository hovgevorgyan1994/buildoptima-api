package com.vecondev.buildoptima.controller;

import com.vecondev.buildoptima.dto.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.response.UserRegistrationResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

public interface RegistrationApi {

  @Operation(summary = "Register new user.")
  ResponseEntity<UserRegistrationResponseDto> register(
      UserRegistrationRequestDto userRegistrationRequestDto, Locale locale);

  @Operation(summary = "Activate email")
  ResponseEntity<UserRegistrationResponseDto> activate(String token);
}

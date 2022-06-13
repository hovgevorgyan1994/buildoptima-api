package com.vecondev.buildoptima.controller;

import com.vecondev.buildoptima.dto.user.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.user.UserRegistrationResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface RegistrationApi {


    @Operation(summary = "Register new user.")
    ResponseEntity<UserRegistrationResponseDto> register(UserRegistrationRequestDto userRegistrationRequestDto);
}

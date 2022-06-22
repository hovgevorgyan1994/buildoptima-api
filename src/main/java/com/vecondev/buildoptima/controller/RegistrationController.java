package com.vecondev.buildoptima.controller;

import com.vecondev.buildoptima.dto.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.response.UserRegistrationResponseDto;
import com.vecondev.buildoptima.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Locale;

@Slf4j
@RestController
@RequestMapping("/registration")
@RequiredArgsConstructor
public class RegistrationController implements RegistrationApi {

  private final RegistrationService service;

  @Override
  @PostMapping
  public ResponseEntity<UserRegistrationResponseDto> register(
      @Valid @RequestBody UserRegistrationRequestDto userRegistrationRequestDto, Locale locale) {
    log.info("Attempt to register new user with email: {}", userRegistrationRequestDto.getEmail());

    return new ResponseEntity<>(
        service.register(userRegistrationRequestDto, locale), HttpStatus.CREATED);
  }

  @Override
  @GetMapping("/activate")
  public ResponseEntity<UserRegistrationResponseDto> activate(@RequestParam("token") String token) {
    log.info("Request to verify an email");

    return ResponseEntity.ok(service.activate(token));
  }
}

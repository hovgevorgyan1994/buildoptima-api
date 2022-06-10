package com.vecondev.buildoptima.controller;

import com.vecondev.buildoptima.dto.request.AuthRequestDto;
import com.vecondev.buildoptima.dto.request.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.response.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.RefreshTokenResponseDto;
import com.vecondev.buildoptima.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class AuthController {
  private final AuthService authService;

  @PostMapping
  public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid AuthRequestDto authRequestDto) {
    log.info("User {} wants to get signed in", authRequestDto.getUsername());
    final AuthResponseDto response = authService.authenticate(authRequestDto);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refreshToken")
  public ResponseEntity<RefreshTokenResponseDto> refreshToken(
      @RequestBody @Valid RefreshTokenRequestDto refreshTokenRequestDto) {
    log.info("Request to refresh the access token");
    final RefreshTokenResponseDto response = authService.refreshToken(refreshTokenRequestDto);

    return ResponseEntity.ok(response);
  }
}

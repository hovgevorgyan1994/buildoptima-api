package com.vecondev.buildoptima.controller.impl;

import com.vecondev.buildoptima.controller.UserApi;
import com.vecondev.buildoptima.dto.request.*;
import com.vecondev.buildoptima.dto.response.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.FetchResponseDto;
import com.vecondev.buildoptima.dto.response.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.response.UserResponseDto;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController implements UserApi {

  private final UserService userService;

  @Override
  @PostMapping("/registration")
  public ResponseEntity<UserResponseDto> register(
      @Valid @RequestBody UserRegistrationRequestDto userRegistrationRequestDto, Locale locale) {
    return new ResponseEntity<>(
        userService.register(userRegistrationRequestDto, locale), HttpStatus.CREATED);
  }

  @Override
  @PutMapping("/activate")
  public ResponseEntity<UserResponseDto> activate(@RequestParam("token") String token) {
    return ResponseEntity.ok(userService.activate(token));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid AuthRequestDto authRequestDto) {
    final AuthResponseDto response = userService.authenticate(authRequestDto);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refreshToken")
  public ResponseEntity<RefreshTokenResponseDto> refreshToken(
      @RequestBody @Valid RefreshTokenRequestDto refreshTokenRequestDto) {
    final RefreshTokenResponseDto response = userService.refreshToken(refreshTokenRequestDto);
    return ResponseEntity.ok(response);
  }

  @Override
  @PostMapping("/fetch")
  public ResponseEntity<FetchResponseDto> fetchUsers(@RequestBody FetchRequestDto fetchRequest) {

    return ResponseEntity.ok(userService.fetchUsers(fetchRequest));
  }

  @Override
  @PutMapping("/password/change")
  public ResponseEntity<Void> changePassword(
      @RequestBody @Valid ChangePasswordRequestDto request,
      @AuthenticationPrincipal AppUserDetails userDetails) {
    userService.changePassword(request, userDetails);
    return ResponseEntity.ok().build();
  }

  @Override
  @GetMapping("fetch/{id}")
  public ResponseEntity<UserResponseDto> getUser(@PathVariable("id") UUID userId) {
    return ResponseEntity.ok(userService.getUser(userId));
  }

  @Override
  @PostMapping("/password/verify")
  public ResponseEntity<Void> forgotPassword(@RequestParam("email") String email, Locale locale) {
    userService.verifyUserAndSendEmail(email, locale);
    return ResponseEntity.ok().build();
  }

  @Override
  @PutMapping("/password/restore")
  public ResponseEntity<Void> restorePassword(
      @RequestBody @Valid RestorePasswordRequestDto restorePasswordRequestDto) {
    userService.restorePassword(restorePasswordRequestDto);
    return ResponseEntity.ok().build();
  }
}

package com.vecondev.buildoptima.controller.impl;

import com.vecondev.buildoptima.controller.UserApi;
import com.vecondev.buildoptima.dto.request.*;
import com.vecondev.buildoptima.dto.response.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.FetchResponse;
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
  @GetMapping("/activate")
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
  public ResponseEntity<FetchResponse> fetchUsers(@RequestBody FetchRequest fetchRequest) {

    return ResponseEntity.ok(userService.fetchUsers(fetchRequest));
  }

  @Override
  @PutMapping("/password/change")
  public ResponseEntity<Void> changePassword(
      @RequestBody @Valid ChangePasswordRequest request,
      @AuthenticationPrincipal AppUserDetails userDetails) {
    userService.changePassword(request, userDetails);
    return ResponseEntity.ok().build();
  }
}

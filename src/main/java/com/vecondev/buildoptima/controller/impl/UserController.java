package com.vecondev.buildoptima.controller.impl;

import com.vecondev.buildoptima.controller.UserApi;
import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.request.user.AuthRequestDto;
import com.vecondev.buildoptima.dto.request.user.ChangePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.user.ConfirmEmailRequestDto;
import com.vecondev.buildoptima.dto.request.user.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.request.user.RestorePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.user.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.response.filter.FetchResponseDto;
import com.vecondev.buildoptima.dto.response.user.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.user.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.response.user.UserResponseDto;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController implements UserApi {

  private final UserService userService;

  @Override
  @PostMapping("/auth/registration")
  public ResponseEntity<UserResponseDto> register(
      @Valid @RequestBody UserRegistrationRequestDto userRegistrationRequestDto, Locale locale) {
    log.info("Attempt to register user with email: {}", userRegistrationRequestDto.getEmail());

    return new ResponseEntity<>(
        userService.register(userRegistrationRequestDto, locale), HttpStatus.CREATED);
  }

  @Override
  @PutMapping("/auth/activate")
  public ResponseEntity<UserResponseDto> activate(@RequestParam("token") String token) {
    return ResponseEntity.ok(userService.activate(token));
  }

  @PostMapping("/auth/login")
  public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid AuthRequestDto authRequestDto) {
    final AuthResponseDto response = userService.authenticate(authRequestDto);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/auth/refresh-token")
  public ResponseEntity<RefreshTokenResponseDto> refreshToken(
      @RequestBody @Valid RefreshTokenRequestDto refreshTokenRequestDto) {
    final RefreshTokenResponseDto response = userService.refreshToken(refreshTokenRequestDto);
    return ResponseEntity.ok(response);
  }

  @Override
  @PostMapping("/fetch")
  @PreAuthorize("hasRole('ADMIN')")
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
  @GetMapping("/{id}")
  public ResponseEntity<UserResponseDto> getUser(@PathVariable("id") UUID userId) {
    return ResponseEntity.ok(userService.getUser(userId));
  }

  @Override
  @PostMapping("/auth/password/receive-email")
  public ResponseEntity<Void> verifyEmail (
      @RequestBody ConfirmEmailRequestDto email, Locale locale) {
    userService.verifyUserAndSendEmail(email, locale);
    return ResponseEntity.ok().build();
  }

  @Override
  @PutMapping("/auth/password/restore")
  public ResponseEntity<Void> restorePassword(
      @RequestBody @Valid RestorePasswordRequestDto restorePasswordRequestDto) {
    userService.restorePassword(restorePasswordRequestDto);
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping(
      value = "/{id}/image",
      consumes = {"multipart/form-data"})
  @PreAuthorize("#user.id == #id")
  public ResponseEntity<Void> uploadImage(
      @PathVariable UUID id,
      @AuthenticationPrincipal AppUserDetails user,
      @RequestParam("file") MultipartFile multipartFile) {
    log.info("Attempt to upload new photo by user with id: {}", id);
    userService.uploadImage(id, multipartFile, user);

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Override
  @GetMapping(value = "/{id}/image")
  @PreAuthorize("#user.id == #ownerId or hasAnyRole('ADMIN')")
  public ResponseEntity<byte[]> downloadOriginalImage(
      @AuthenticationPrincipal AppUserDetails user, @PathVariable("id") UUID ownerId) {
    log.info(
        "User with id: {} trying to download original image of user with id: {}.",
        user.getId(),
        ownerId);

    return userService.downloadImage(ownerId, true);
  }

  @Override
  @GetMapping(value = "/{id}/thumbnail-image")
  @PreAuthorize("#user.id == #ownerId or hasAnyRole('ADMIN')")
  public ResponseEntity<byte[]> downloadThumbnailImage(
      @AuthenticationPrincipal AppUserDetails user, @PathVariable("id") UUID ownerId) {
    log.info(
        "User with id: {} trying to download thumbnail image of user with id: {}.",
        user.getId(),
        ownerId);

    return userService.downloadImage(ownerId, false);
  }

  @Override
  @DeleteMapping(value = "/{id}/image")
  @PreAuthorize("#user.id == #ownerId or hasAnyRole('ADMIN')")
  public ResponseEntity<Void> deleteImage(
      @AuthenticationPrincipal AppUserDetails user, @PathVariable("id") UUID ownerId) {
    userService.deleteImage(ownerId);

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}

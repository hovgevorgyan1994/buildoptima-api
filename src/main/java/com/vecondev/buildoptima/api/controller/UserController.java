package com.vecondev.buildoptima.api.controller;

import com.vecondev.buildoptima.api.UserApi;
import com.vecondev.buildoptima.dto.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.filter.FetchResponseDto;
import com.vecondev.buildoptima.dto.user.request.ChangePasswordRequestDto;
import com.vecondev.buildoptima.dto.user.response.UserResponseDto;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.auth.SecurityContextService;
import com.vecondev.buildoptima.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController implements UserApi {

  private final UserService userService;
  private final SecurityContextService securityContextService;

  @Override
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponseDto> getById(@PathVariable("id") UUID userId) {
    return ResponseEntity.ok(userService.getById(userId));
  }

  @Override
  @PostMapping("/fetch")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<FetchResponseDto> fetch(@RequestBody FetchRequestDto fetchRequest) {

    return ResponseEntity.ok(userService.fetch(fetchRequest));
  }

  @Override
  @PutMapping("/password/change")
  public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequestDto request) {
    userService.changePassword(request);
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping(
      value = "/{id}/image",
      consumes = {"multipart/form-data"})
  @PreAuthorize("#user.id == #id")
  public ResponseEntity<Void> uploadImage(
      @PathVariable UUID id,
      @RequestParam("file") MultipartFile multipartFile,
      @AuthenticationPrincipal AppUserDetails user) {
    log.info("Attempt to upload new photo by user with id: {}", id);
    userService.uploadImage(id, multipartFile);

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Override
  @GetMapping(value = "/{id}/image")
  @PreAuthorize("#user.id == #ownerId or hasAnyRole('ADMIN')")
  public ResponseEntity<byte[]> downloadOriginalImage(
      @PathVariable("id") UUID ownerId, @AuthenticationPrincipal AppUserDetails user) {
    String username = securityContextService.getUserDetails().getUsername();
    log.info("User {} trying to download original image of user with id: {}.", username, ownerId);

    return userService.downloadImage(ownerId, true);
  }

  @Override
  @GetMapping(value = "/{id}/thumbnail-image")
  @PreAuthorize("#user.id == #ownerId or hasAnyRole('ADMIN')")
  public ResponseEntity<byte[]> downloadThumbnailImage(
      @PathVariable("id") UUID ownerId, @AuthenticationPrincipal AppUserDetails user) {
    String username = securityContextService.getUserDetails().getUsername();
    log.info("User {} trying to download thumbnail image of user with id: {}.", username, ownerId);

    return userService.downloadImage(ownerId, false);
  }

  @Override
  @DeleteMapping(value = "/{id}/image")
  @PreAuthorize("#user.id == #ownerId or hasAnyRole('ADMIN')")
  public ResponseEntity<Void> deleteImage(
      @PathVariable("id") UUID ownerId, @AuthenticationPrincipal AppUserDetails user) {
    userService.deleteImage(ownerId);

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}

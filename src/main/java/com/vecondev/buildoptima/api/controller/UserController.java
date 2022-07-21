package com.vecondev.buildoptima.api.controller;

import com.vecondev.buildoptima.api.UserApi;
import com.vecondev.buildoptima.dto.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.filter.FetchResponseDto;
import com.vecondev.buildoptima.dto.user.request.ChangePasswordRequestDto;
import com.vecondev.buildoptima.dto.user.response.UserResponseDto;
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
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController implements UserApi {

  private final UserService userService;

  @Override
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponseDto> getById(@PathVariable("id") UUID userId) {
    return ResponseEntity.ok(userService.getById(userId));
  }

  @Override
  @PostMapping("/fetch")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<FetchResponseDto> fetch(
      @RequestBody FetchRequestDto fetchRequest, @AuthenticationPrincipal AppUserDetails user) {

    return ResponseEntity.ok(userService.fetch(fetchRequest, user.getUsername()));
  }

  @Override
  @PutMapping("/password/change")
  public ResponseEntity<Void> changePassword(
      @RequestBody @Valid ChangePasswordRequestDto request,
      @AuthenticationPrincipal AppUserDetails user) {
    userService.changePassword(request, user);
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

package com.vecondev.buildoptima.service;


import com.vecondev.buildoptima.dto.request.AuthRequestDto;
import com.vecondev.buildoptima.dto.request.ConfirmEmailRequestDto;
import com.vecondev.buildoptima.dto.request.FetchRequestDto;
import com.vecondev.buildoptima.dto.request.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.request.ChangePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.RestorePasswordRequestDto;

import com.vecondev.buildoptima.dto.response.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.FetchResponseDto;
import com.vecondev.buildoptima.dto.response.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.response.UserResponseDto;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.UUID;

public interface UserService {

  UserResponseDto register(UserRegistrationRequestDto dto, Locale locale);

  UserResponseDto activate(String token);

  AuthResponseDto authenticate(final AuthRequestDto authRequestDto);

  RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto);

  FetchResponseDto fetchUsers(FetchRequestDto viewRequest);;

  void changePassword(ChangePasswordRequestDto request, AppUserDetails userDetails);

  UserResponseDto getUser(UUID userId);

  void verifyUserAndSendEmail(ConfirmEmailRequestDto email, Locale locale);

  void restorePassword(RestorePasswordRequestDto restorePasswordRequestDto);


  void uploadImage(UUID userId, MultipartFile multipartFile);

  ResponseEntity<byte[]> downloadImage(UUID userId, UUID ownerId, boolean isOriginal);

  void deleteImage(UUID userId);
}

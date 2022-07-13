package com.vecondev.buildoptima.service.user;


import com.vecondev.buildoptima.dto.request.user.AuthRequestDto;
import com.vecondev.buildoptima.dto.request.user.ChangePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.user.ConfirmEmailRequestDto;
import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.request.user.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.request.user.RestorePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.user.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.response.user.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.filter.FetchResponseDto;
import com.vecondev.buildoptima.dto.response.user.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.response.user.UserResponseDto;
import com.vecondev.buildoptima.model.user.User;
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

  FetchResponseDto fetchUsers(FetchRequestDto viewRequest);

  void changePassword(ChangePasswordRequestDto request, AppUserDetails userDetails);

  UserResponseDto getUser(UUID userId);

  void verifyUserAndSendEmail(ConfirmEmailRequestDto email, Locale locale);

  void restorePassword(RestorePasswordRequestDto restorePasswordRequestDto);

  void uploadImage(UUID userId, MultipartFile multipartFile);

  ResponseEntity<byte[]> downloadImage(UUID userId, UUID ownerId, boolean isOriginal);

  void deleteImage(UUID userId);

  User getUserById(UUID userId);
}

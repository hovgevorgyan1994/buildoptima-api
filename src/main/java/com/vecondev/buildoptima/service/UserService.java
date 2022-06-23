package com.vecondev.buildoptima.service;

import com.vecondev.buildoptima.dto.request.*;
import com.vecondev.buildoptima.dto.response.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.response.UserResponseDto;
import com.vecondev.buildoptima.dto.response.FetchResponseDto;
import com.vecondev.buildoptima.security.user.AppUserDetails;

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

  void verifyUserAndSendEmail(String email, Locale locale);

  void restorePassword(RestorePasswordRequestDto restorePasswordRequestDto);
}

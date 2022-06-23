package com.vecondev.buildoptima.service;

import com.vecondev.buildoptima.dto.request.*;
import com.vecondev.buildoptima.dto.response.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.response.UserResponseDto;
import com.vecondev.buildoptima.dto.response.FetchResponse;
import com.vecondev.buildoptima.security.user.AppUserDetails;

import java.util.Locale;
import java.util.UUID;

public interface UserService {

  UserResponseDto register(UserRegistrationRequestDto dto, Locale locale);

  UserResponseDto activate(String token);

  AuthResponseDto authenticate(final AuthRequestDto authRequestDto);

  RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto);

  FetchResponse fetchUsers(FetchRequest viewRequest);

  void changePassword(ChangePasswordRequest request, AppUserDetails userDetails);

  UserResponseDto getUser(UUID userId);
}

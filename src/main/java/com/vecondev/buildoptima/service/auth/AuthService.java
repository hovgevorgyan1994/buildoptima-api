package com.vecondev.buildoptima.service.auth;

import com.vecondev.buildoptima.dto.user.request.AuthRequestDto;
import com.vecondev.buildoptima.dto.user.request.ConfirmEmailRequestDto;
import com.vecondev.buildoptima.dto.user.request.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.user.request.RestorePasswordRequestDto;
import com.vecondev.buildoptima.dto.user.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.user.response.AuthResponseDto;
import com.vecondev.buildoptima.dto.user.response.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.user.response.UserResponseDto;
import com.vecondev.buildoptima.model.user.User;
import java.util.Locale;

public interface AuthService {
  UserResponseDto register(UserRegistrationRequestDto dto, Locale locale);

  void sendEmail(Locale locale, User user);

  UserResponseDto activate(String token);

  AuthResponseDto login(final AuthRequestDto authRequestDto);

  RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto);

  void verify(ConfirmEmailRequestDto email, Locale locale);

  void restorePassword(RestorePasswordRequestDto restorePasswordRequestDto);
}

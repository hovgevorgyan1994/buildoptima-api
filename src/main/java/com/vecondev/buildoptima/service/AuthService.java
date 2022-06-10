package com.vecondev.buildoptima.service;

import com.vecondev.buildoptima.dto.request.AuthRequestDto;
import com.vecondev.buildoptima.dto.request.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.response.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.RefreshTokenResponseDto;

public interface AuthService {

  AuthResponseDto authenticate(final AuthRequestDto authRequestDto);

  RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto);
}

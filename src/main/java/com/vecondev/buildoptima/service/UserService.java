package com.vecondev.buildoptima.service;

import com.vecondev.buildoptima.dto.request.AuthRequestDto;
import com.vecondev.buildoptima.dto.request.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.request.UsersViewRequest;
import com.vecondev.buildoptima.dto.response.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.response.UserResponseDto;
import com.vecondev.buildoptima.dto.response.UsersViewResponse;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

public interface UserService {

    UserResponseDto register(UserRegistrationRequestDto dto, Locale locale);

    UserResponseDto activate(String token);

    AuthResponseDto authenticate(final AuthRequestDto authRequestDto);

    RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto);


    UsersViewResponse getUsers(UsersViewRequest viewRequest);
}

package com.vecondev.buildoptima.controller;

import com.vecondev.buildoptima.dto.request.AuthRequestDto;
import com.vecondev.buildoptima.dto.request.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.request.UsersViewRequest;
import com.vecondev.buildoptima.dto.response.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.response.UserResponseDto;
import com.vecondev.buildoptima.dto.response.UsersViewResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

public interface UserApi {


    @Operation(summary = "Register new user.")
    ResponseEntity<UserResponseDto> register(
            UserRegistrationRequestDto userRegistrationRequestDto, Locale locale);

    @Operation(summary = "Activate email")
    ResponseEntity<UserResponseDto> activate(String token);


    @Operation(summary = "User sign in")
    ResponseEntity<AuthResponseDto> login(AuthRequestDto authRequestDto);



    @Operation(summary = "Refresh user token")
    ResponseEntity<RefreshTokenResponseDto> refreshToken(RefreshTokenRequestDto refreshTokenRequestDto);


    @Operation(summary = "Fetch users sorted paged and sorted")
    ResponseEntity<UsersViewResponse> getUsers(UsersViewRequest viewRequest);







}

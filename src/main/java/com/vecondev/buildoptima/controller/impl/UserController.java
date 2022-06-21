package com.vecondev.buildoptima.controller.impl;

import com.vecondev.buildoptima.controller.UserApi;
import com.vecondev.buildoptima.dto.request.AuthRequestDto;
import com.vecondev.buildoptima.dto.request.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.request.UsersViewRequest;
import com.vecondev.buildoptima.dto.response.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.response.UserResponseDto;
import com.vecondev.buildoptima.dto.response.UsersViewResponse;
import com.vecondev.buildoptima.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Locale;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController implements UserApi {

    private final UserService userService;


    @Override
    @PostMapping
    public ResponseEntity<UserResponseDto> register(
            @Valid @RequestBody UserRegistrationRequestDto userRegistrationRequestDto, Locale locale) {
        log.info("Attempt to register new user with email: {}", userRegistrationRequestDto.getEmail());

        return new ResponseEntity<>(
                userService.register(userRegistrationRequestDto, locale), HttpStatus.CREATED);
    }


    @Override
    @GetMapping("/activate")
    public ResponseEntity<UserResponseDto> activate(@RequestParam("token") String token) {
        log.info("Request to verify an email");

        return ResponseEntity.ok(userService.activate(token));
    }



    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid AuthRequestDto authRequestDto) {
        log.info("User {} wants to get signed in", authRequestDto.getUsername());
        final AuthResponseDto response = userService.authenticate(authRequestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(
            @RequestBody @Valid RefreshTokenRequestDto refreshTokenRequestDto) {
        log.info("Request to refresh the access token");
        final RefreshTokenResponseDto response = userService.refreshToken(refreshTokenRequestDto);

        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/view")
    public ResponseEntity<UsersViewResponse> getUsers(@RequestBody UsersViewRequest viewRequest) {

        return ResponseEntity.ok(userService.getUsers(viewRequest));
    }
}

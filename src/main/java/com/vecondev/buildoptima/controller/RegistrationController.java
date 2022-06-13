package com.vecondev.buildoptima.controller;

import com.vecondev.buildoptima.dto.user.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.user.UserRegistrationResponseDto;
import com.vecondev.buildoptima.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/registration")
@RequiredArgsConstructor
public class RegistrationController implements RegistrationApi {

    private final RegistrationService service;

    @Override
    @PostMapping
    public ResponseEntity<UserRegistrationResponseDto> register(@Valid @RequestBody UserRegistrationRequestDto userRegistrationRequestDto){
        log.info("Attempt to register new user with email: {}", userRegistrationRequestDto.getEmail());

        return new ResponseEntity<>(service.register(userRegistrationRequestDto), HttpStatus.CREATED);
    }

}


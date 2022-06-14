package com.vecondev.buildoptima.service;

import com.vecondev.buildoptima.dto.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.response.UserRegistrationResponseDto;

import java.util.Locale;

public interface RegistrationService {

    UserRegistrationResponseDto register(UserRegistrationRequestDto dto, Locale locale);

    UserRegistrationResponseDto activate(String token);

}

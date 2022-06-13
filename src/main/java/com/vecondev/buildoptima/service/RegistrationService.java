package com.vecondev.buildoptima.service;

import com.vecondev.buildoptima.dto.user.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.user.UserRegistrationResponseDto;

public interface RegistrationService {

    UserRegistrationResponseDto register(UserRegistrationRequestDto dto);
}

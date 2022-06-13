package com.vecondev.buildoptima.service.impl;

import com.vecondev.buildoptima.dto.user.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.user.UserRegistrationResponseDto;
import com.vecondev.buildoptima.mapper.user.UserMapper;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.repository.UserRepository;
import com.vecondev.buildoptima.service.RegistrationService;
import com.vecondev.buildoptima.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final UserValidator userValidator;

    @Override
    public UserRegistrationResponseDto register(UserRegistrationRequestDto dto) {
        User user = userMapper.mapToEntity(dto);
        userValidator.validateUserRegistration(user);
        user = userRepository.save(user);
        log.info("New user registered.");

        return userMapper.mapToRegistrationResponseDto(user);
    }
}

package com.vecondev.buildoptima.mapper.user;

import com.vecondev.buildoptima.dto.user.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.user.UserRegistrationResponseDto;
import com.vecondev.buildoptima.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.vecondev.buildoptima.model.user.Role.CLIENT;

public class UserMapperDecorator implements UserMapper {

    @Autowired
    @Qualifier("delegate")
    private UserMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User mapToEntity(UserRegistrationRequestDto dto) {
    return mapper.mapToEntity(dto).toBuilder()
        .password(passwordEncoder.encode(dto.getPassword()))
        .role(CLIENT)
        .enabled(false)
        .build();
    }

    @Override
    public UserRegistrationResponseDto mapToRegistrationResponseDto(User user) {
        return mapper.mapToRegistrationResponseDto(user);
    }
}

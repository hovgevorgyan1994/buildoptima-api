package com.vecondev.buildoptima.mapper.user;

import com.vecondev.buildoptima.dto.request.user.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.response.user.UserOverview;
import com.vecondev.buildoptima.dto.response.user.UserResponseDto;
import com.vecondev.buildoptima.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static com.vecondev.buildoptima.model.user.Role.CLIENT;

public abstract class UserMapperDecorator implements UserMapper {

  @Autowired
  @Qualifier("delegate")
  private UserMapper mapper;

  @Autowired private PasswordEncoder passwordEncoder;

  @Override
  public User mapToEntity(UserRegistrationRequestDto dto) {
    return mapper.mapToEntity(dto).toBuilder()
        .password(passwordEncoder.encode(dto.getPassword()))
        .role(CLIENT)
        .enabled(false)
        .build();
  }

  @Override
  public UserResponseDto mapToResponseDto(User user) {
    return mapper.mapToResponseDto(user).toBuilder()
            .id(user.getId())
            .build();
  }

  public List<UserResponseDto> mapToResponseList(Page<User> users) {
    return mapper.mapToResponseList(users);
  }

  @Override
  public UserOverview mapToOverview(User user) {
    return mapper.mapToOverview(user);
  }
}

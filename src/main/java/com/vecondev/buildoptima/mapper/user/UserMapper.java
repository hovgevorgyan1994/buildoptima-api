package com.vecondev.buildoptima.mapper.user;

import com.vecondev.buildoptima.dto.user.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.EntityOverview;
import com.vecondev.buildoptima.dto.user.response.UserResponseDto;
import com.vecondev.buildoptima.model.user.User;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@DecoratedWith(UserMapperDecorator.class)
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    User mapToEntity(UserRegistrationRequestDto dto);

    UserResponseDto mapToResponseDto(User user);

    List<UserResponseDto> mapToResponseList(Page<User> users);

  EntityOverview mapToOverview(User user);
}

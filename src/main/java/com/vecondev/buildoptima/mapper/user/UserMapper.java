package com.vecondev.buildoptima.mapper.user;

import com.vecondev.buildoptima.dto.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.response.UserRegistrationResponseDto;
import com.vecondev.buildoptima.model.user.User;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@DecoratedWith(UserMapperDecorator.class)
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    User mapToEntity(UserRegistrationRequestDto dto);

    UserRegistrationResponseDto mapToRegistrationResponseDto(User user);
}

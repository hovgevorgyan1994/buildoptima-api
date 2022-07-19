package com.vecondev.buildoptima.filter.converter;

import com.vecondev.buildoptima.exception.InvalidFieldException;
import com.vecondev.buildoptima.model.user.Role;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.lang.Nullable;

import static com.vecondev.buildoptima.exception.ErrorCode.INVALID_ROLE;

public class RoleConverter implements Converter<String, Role> {

  @Nullable
  @Override
  public Role convert(@NonNull String role) {
    try {
      return Role.valueOf(role);
    } catch (InvalidDataAccessApiUsageException ex) {
      throw new InvalidFieldException(INVALID_ROLE);
    }
  }
}

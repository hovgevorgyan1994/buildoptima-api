package com.vecondev.buildoptima.filter.converter;

import com.vecondev.buildoptima.model.user.Role;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

public class RoleConverter implements Converter<String, Role> {
  @Nullable
  @Override
  public Role convert(@NonNull String role) {
    return Role.valueOf(role);
  }
}

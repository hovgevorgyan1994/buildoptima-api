package com.vecondev.buildoptima.filter.converter;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;

import java.util.UUID;

public class UUIDConverter implements Converter<String, UUID> {
  @Override
  public UUID convert(@NonNull String userId) {
    return UUID.fromString(userId);
  }
}

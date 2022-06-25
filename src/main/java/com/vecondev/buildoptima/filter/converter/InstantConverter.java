package com.vecondev.buildoptima.filter.converter;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;

import java.time.Instant;

public class InstantConverter implements Converter<String, Instant> {

  @Override
  public Instant convert(@NonNull String date) {
    return Instant.parse(date);
  }
}

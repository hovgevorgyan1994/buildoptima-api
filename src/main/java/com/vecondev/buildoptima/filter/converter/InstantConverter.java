package com.vecondev.buildoptima.filter.converter;

import com.vecondev.buildoptima.exception.InvalidFieldException;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import static com.vecondev.buildoptima.exception.Error.INVALID_INSTANT;

public class InstantConverter implements Converter<String, Instant> {

  @Override
  public Instant convert(@NonNull String date) {
    try {
    return Instant.parse(date);
    } catch (DateTimeParseException ex) {
      throw new InvalidFieldException(INVALID_INSTANT);
    }
  }
}

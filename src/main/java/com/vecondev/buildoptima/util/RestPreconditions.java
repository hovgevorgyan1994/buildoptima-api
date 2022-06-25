package com.vecondev.buildoptima.util;

import com.vecondev.buildoptima.exception.ResourceNotFoundException;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;

import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class RestPreconditions {

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * @param message the message of the exception if the check fails
   * @throws ResourceNotFoundException if {@code object} is null
   */
  public static <T> void checkNotNull(final T object, final String message, final HttpStatus status) {
    if (Objects.isNull(object)) {
      throw new ResourceNotFoundException(message, status);
    }
  }

  /**
   * Ensures that a string passed as a parameter to the calling method is not null or blank
   *
   * @param message the message of the exception if the check fails
   * @throws ResourceNotFoundException if {@code object} is null
   */
  public static void checkNotBlank(final String str, final String message, final HttpStatus status) {
    if (Strings.isEmpty(str)) {
      throw new ResourceNotFoundException(message,status);
    }
  }
}

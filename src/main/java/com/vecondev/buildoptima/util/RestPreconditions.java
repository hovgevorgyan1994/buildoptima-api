package com.vecondev.buildoptima.util;

import com.vecondev.buildoptima.exception.ErrorCode;
import com.vecondev.buildoptima.exception.ResourceNotFoundException;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class RestPreconditions {

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * @throws ResourceNotFoundException if {@code object} is null
   */
  public static <T> void checkNotNull(final T object, final ErrorCode errorCode) {
    if (Objects.isNull(object)) {
      throw new ResourceNotFoundException(errorCode);
    }
  }

  /**
   * Ensures that a string passed as a parameter to the calling method is not null or blank
   *
   * @throws ResourceNotFoundException if {@code object} is null
   */
  public static void checkNotBlank(final String str, final ErrorCode errorCode) {
    if (Strings.isEmpty(str)) {
      throw new ResourceNotFoundException(errorCode);
    }
  }
}

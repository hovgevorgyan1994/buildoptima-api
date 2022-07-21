package com.vecondev.buildoptima.validation.validator;

import com.vecondev.buildoptima.exception.Error;
import com.vecondev.buildoptima.exception.InvalidFieldException;
import com.vecondev.buildoptima.filter.model.SortDto;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import static com.vecondev.buildoptima.exception.Error.*;

public class FieldNameValidator {

  private FieldNameValidator() {}

  public static void validateFieldNames(
      Map<String, ?> fieldDefinitionMap, @NotNull List<SortDto> sort) {
    sort.forEach(
        sortDto -> {
          if (!fieldDefinitionMap.containsKey(sortDto.getField())) {
            throw new InvalidFieldException(INVALID_SORTING_FIELD);
          }
        });
  }
}

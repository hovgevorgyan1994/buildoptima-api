package com.vecondev.buildoptima.validation.validator;

import com.vecondev.buildoptima.validation.constraint.NullOrNotEmptyList;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class NullOrNotEmptyListValidator
    implements ConstraintValidator<NullOrNotEmptyList, List<Object>> {

  @Override
  public boolean isValid(List<Object> list, ConstraintValidatorContext context) {
    return list == null || !list.isEmpty();
  }
}

package com.vecondev.buildoptima.validation.validator;

import com.vecondev.buildoptima.validation.constraint.NullOrNotEmptyList;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class NullOrNotEmptyListValidator
    implements ConstraintValidator<NullOrNotEmptyList, List<String>> {

  @Override
  public boolean isValid(List<String> list, ConstraintValidatorContext context) {
    return list == null || !list.isEmpty();
  }
}

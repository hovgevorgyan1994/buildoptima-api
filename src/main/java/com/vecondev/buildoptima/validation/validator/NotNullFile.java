package com.vecondev.buildoptima.validation.validator;

import com.vecondev.buildoptima.validation.constraint.ValidImage;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotNullFile implements ConstraintValidator<ValidImage, MultipartFile> {

  @Override
  public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
    return value == null || !value.isEmpty();
  }
}

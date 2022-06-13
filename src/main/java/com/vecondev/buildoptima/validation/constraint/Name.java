package com.vecondev.buildoptima.validation.constraint;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.lang.annotation.*;

@NotBlank(message = "Can't be empty!")
@Size(min = 2, max = 20, message = "The length should be between 2 and 20 characters!")
@Pattern(regexp = "^[A-Za-z]*$", message = "Should contain only letters!")
@Documented
@Constraint(validatedBy = {})
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Name {

    String message() default "Invalid name!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

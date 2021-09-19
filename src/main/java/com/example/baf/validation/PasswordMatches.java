package com.example.baf.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ANNOTATION_TYPE})
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Documented
public @interface PasswordMatches {

    String message() default "Password do not match";

    Class<?>[] group() default {};

    Class<? extends Payload>[] payload() default {};
}

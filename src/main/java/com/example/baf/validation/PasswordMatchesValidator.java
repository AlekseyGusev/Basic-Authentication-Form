package com.example.baf.validation;

import com.example.baf.model.User;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {
    @Override
    public void initialize(PasswordMatches constraintAnnotation) {

    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        User user = (User) value;
        return Objects.equals(user.getPassword(), user.getPasswordConfirmation());
    }
}

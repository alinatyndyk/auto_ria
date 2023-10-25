package com.example.auto_ria.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<ValidRole, Enum<?>> {

    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(ValidRole constraintAnnotation) {
        enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(Enum<?> value, ConstraintValidatorContext context) {

        // Check if the value is one of the enum values
        for (Enum<?> enumValue : enumClass.getEnumConstants()) {
            if (enumValue.equals(value)) {
                return true;
            }
        }

        // Value does not match any of the enum values, throw an error
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Invalid role value. Must be one of " + Arrays.toString(enumClass.getEnumConstants()))
                .addConstraintViolation();
        return false;
    }
}

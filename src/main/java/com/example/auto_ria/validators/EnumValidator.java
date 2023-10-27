package com.example.auto_ria.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

public class EnumValidator implements ConstraintValidator<ValidRole, String>, ValueExtractor<Object> {

    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(ValidRole constraintAnnotation) {
        enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Null values are considered valid
        }

        Enum<?>[] enumValues = enumClass.getEnumConstants();
        for (Enum<?> enumValue : enumValues) {
            if (enumValue.name().equals(value)) {
                return true; // Value matches one of the enum constants
            }
        }
        // Throw IllegalArgumentException if value does not match any enum constant
        throw new IllegalArgumentException("Invalid enum value: " + value);
    }

    @Override
    public void extractValues(Object value, ValueReceiver receiver) {
        if (value instanceof Enum<?> enumValue) {
            receiver.value(null, enumValue.name());
        }
    }

    @ExtractedValue
    public static Class<?> getValidatedValueType(EnumValidator extractor, ValidRole annotation) {
        return Enum.class;
    }
}

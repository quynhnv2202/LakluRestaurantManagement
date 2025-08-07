package com.laklu.pos.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class EnumValidator  implements ConstraintValidator<ValidEnum, String> {
    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false; // Không chấp nhận giá trị null
        }

        return Arrays.stream(enumClass.getEnumConstants())
            .map(Enum::name)
            .anyMatch(enumValue -> enumValue.equalsIgnoreCase(value)); // Cho phép nhập không phân biệt hoa thường
    }
}

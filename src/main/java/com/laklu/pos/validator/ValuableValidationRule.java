package com.laklu.pos.validator;

import lombok.Getter;

import java.util.Optional;
import java.util.function.Supplier;

@Getter
public class ValuableValidationRule<T> implements Rule {
    private final String field;
    private final String message;
    private final Supplier<Optional<T>> supplier;
    private T value;

    public ValuableValidationRule(String field, String message, Supplier<Optional<T>> supplier) {
        this.field = field;
        this.message = message;
        this.supplier = supplier;
    }

    @Override
    public String field() {
        return field;
    }

    @Override
    public boolean isValid() {
        Optional<T> optionalValue = supplier.get();
        if (optionalValue.isPresent()) {
            this.value = optionalValue.get();
            return true;
        }
        return false;
    }

    @Override
    public String getMessage() {
        return message;
    }
}





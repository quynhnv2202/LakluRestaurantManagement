package com.laklu.pos.validator;

import lombok.AllArgsConstructor;

import java.util.function.Function;
@AllArgsConstructor
public class ValidationRule implements Rule {

    private Function<Void, Boolean> function;
    private String field;
    private String message;

    @Override
    public String field() {
        return field;
    }

    @Override
    public boolean isValid() {
        return this.function.apply(null);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
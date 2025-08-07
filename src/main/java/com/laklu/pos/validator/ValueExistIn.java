package com.laklu.pos.validator;

import java.util.function.Function;

public class ValueExistIn<T> extends BaseRule {
    private final T fieldValue;
    private final Function<T, Boolean> fieldValueResolver;
    private final String field;

    public ValueExistIn(String field, T fieldValue, Function<T, Boolean> fieldValueResolver) {
        this.fieldValue = fieldValue;
        this.fieldValueResolver = fieldValueResolver;
        this.field = field;
    }

    @Override
    public String getValidateField() {
        return this.field;
    }

    @Override
    public boolean isValid() {
        return fieldValueResolver.apply(fieldValue);
    }

    @Override
    public String getMessage() {
        return field + " đã tồn tại";
    }
}

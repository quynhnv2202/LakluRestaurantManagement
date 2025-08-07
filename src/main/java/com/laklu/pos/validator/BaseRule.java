package com.laklu.pos.validator;

import lombok.Data;

@Data
public abstract class BaseRule implements Rule{
    
    private String message;

    @Override
    public String field() {
        return this.getValidateField();
    }

    public abstract String getValidateField();
}

package com.laklu.pos.exceptions;

import lombok.Getter;

@Getter
public class RuleNotValidException extends RestHttpException {
    private final Object errors;

    public RuleNotValidException(Object errors) {
        super(ExceptionCode.UNPROCESSABLE_ENTITY);
        this.errors = errors;
    }
}

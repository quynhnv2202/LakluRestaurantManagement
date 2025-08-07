package com.laklu.pos.validator;

public interface Rule {
    String field();

    boolean isValid();

    String getMessage();
}

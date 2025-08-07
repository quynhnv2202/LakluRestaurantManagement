package com.laklu.pos.exceptions;

import lombok.Getter;

@Getter
public class InvalidCredentialsException extends RestHttpException {
    private final ExceptionCode exceptionCode;

    public InvalidCredentialsException() {
        super(ExceptionCode.INVALID_CREDENTIALS);
        this.exceptionCode = ExceptionCode.UNAUTHORIZED;
    }
}

package com.laklu.pos.exceptions;

import lombok.Getter;

@Getter
public class InvalidTokenException extends RestHttpException {
    private final ExceptionCode exceptionCode;

    public InvalidTokenException() {
        super(ExceptionCode.INVALID_TOKEN);
        this.exceptionCode = ExceptionCode.UNAUTHORIZED;
    }
}

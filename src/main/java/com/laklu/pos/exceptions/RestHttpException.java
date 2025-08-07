package com.laklu.pos.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public abstract class RestHttpException extends RuntimeException {

    protected ExceptionCode exceptionCode;
    protected HttpStatusCode statusCode;
    protected int code;

    public RestHttpException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
        this.statusCode = exceptionCode.getStatusCode();
        this.code = exceptionCode.getCode();
    }
}

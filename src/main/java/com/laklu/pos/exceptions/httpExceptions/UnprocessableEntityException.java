package com.laklu.pos.exceptions.httpExceptions;

import com.laklu.pos.exceptions.ExceptionCode;
import com.laklu.pos.exceptions.RestHttpException;


public class UnprocessableEntityException extends RestHttpException {
    public UnprocessableEntityException() {
        super(ExceptionCode.UNPROCESSABLE_ENTITY);
    }
}

package com.laklu.pos.exceptions.httpExceptions;

import com.laklu.pos.exceptions.ExceptionCode;
import com.laklu.pos.exceptions.RestHttpException;

public class BadRequestException extends RestHttpException {
    public BadRequestException() {
        super(ExceptionCode.BAD_REQUEST);
    }
}

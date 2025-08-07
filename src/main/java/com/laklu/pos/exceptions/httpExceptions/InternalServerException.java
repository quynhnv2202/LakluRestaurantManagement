package com.laklu.pos.exceptions.httpExceptions;

import com.laklu.pos.exceptions.ExceptionCode;
import com.laklu.pos.exceptions.RestHttpException;
import lombok.Getter;

@Getter
public class InternalServerException extends RestHttpException {

    public InternalServerException() {
        super(ExceptionCode.INTERNAL_SERVER_ERROR);
    }
}

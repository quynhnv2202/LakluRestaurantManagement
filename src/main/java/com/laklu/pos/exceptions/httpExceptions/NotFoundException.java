package com.laklu.pos.exceptions.httpExceptions;

import com.laklu.pos.exceptions.ExceptionCode;
import com.laklu.pos.exceptions.RestHttpException;
import lombok.Getter;

@Getter
public class NotFoundException extends RestHttpException {
    public NotFoundException() {
        super(ExceptionCode.NOT_FOUND);
    }
}

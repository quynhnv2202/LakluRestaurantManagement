package com.laklu.pos.exceptions.httpExceptions;

import com.laklu.pos.exceptions.ExceptionCode;
import com.laklu.pos.exceptions.RestHttpException;
import lombok.Getter;

@Getter
public class ForbiddenException extends RestHttpException {
    public ForbiddenException() {
        super(ExceptionCode.FORBIDDEN);
    }
}

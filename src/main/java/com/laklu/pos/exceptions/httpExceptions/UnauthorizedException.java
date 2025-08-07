package com.laklu.pos.exceptions.httpExceptions;

import com.laklu.pos.exceptions.ExceptionCode;
import com.laklu.pos.exceptions.RestHttpException;
import lombok.Getter;

@Getter
public class UnauthorizedException extends RestHttpException {
    public UnauthorizedException() {
        super(ExceptionCode.UNAUTHORIZED);
    }
}

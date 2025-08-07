package com.laklu.pos.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserInactiveException extends RuntimeException {
    public UserInactiveException() {
        super("Tài khoản không trong trạng thái làm việc");
    }

    public UserInactiveException(String message) {
        super(message);
    }
} 
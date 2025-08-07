package com.laklu.pos.validator;

import com.laklu.pos.entities.User;
import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.function.Function;

@AllArgsConstructor
public class UsernameMustBeUnique extends BaseRule {
    private final Function<String, Optional<User>> userResolver;
    private final String username;

    @Override
    public String getValidateField() {
        return "username";
    }

    @Override
    public boolean isValid() {
        return userResolver.apply(username).isEmpty();
    }

    @Override
    public String getMessage() {
        return "Người dùng đã tồn tại";
    }
}

package com.laklu.pos.validator;

import com.laklu.pos.entities.Menu;
import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.function.Function;

@AllArgsConstructor
public class MenuNameMustBeUnique extends BaseRule {
    private final Function<String, Optional<Menu>> menuResolver;
    private final String menuName;

    @Override
    public String getValidateField() {
        return "menuName";
    }

    @Override
    public boolean isValid() {
        return menuResolver.apply(menuName).isEmpty();
    }

    @Override
    public String getMessage() {
        return "Tên menu đã tồn tại";
    }
} 
package com.laklu.pos.validator;

import com.laklu.pos.entities.Table;
import lombok.AllArgsConstructor;


import java.util.Optional;
import java.util.function.Function;

@AllArgsConstructor
public class TableMustBeUnique extends BaseRule {
    private final Function<String, Optional<Table>> tableResolver;
    private final String tableName;

    @Override
    public String getValidateField() {
        return "tableName";
    }

    @Override
    public boolean isValid() {
        return tableResolver.apply(tableName).isEmpty();
    }

    @Override
    public String getMessage() {
        return "Tên bàn đã tồn tại";
    }
}

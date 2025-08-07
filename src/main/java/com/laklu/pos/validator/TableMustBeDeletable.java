package com.laklu.pos.validator;

import com.laklu.pos.entities.Table;
import com.laklu.pos.enums.StatusTable;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TableMustBeDeletable extends BaseRule {
    private final Table table;

    @Override
    public String getValidateField() {
        return "tableStatus";
    }

    @Override
    public boolean isValid() {
        return !(table.getStatus() == StatusTable.RESERVED || table.getStatus() == StatusTable.OCCUPIED);
    }

    @Override
    public String getMessage() {
        return "Không thể xóa bàn đang được đặt trước hoặc đang có khách sử dụng.";
    }
}
package com.laklu.pos.validator;

import com.laklu.pos.entities.Table;
import com.laklu.pos.services.ReservationByDateResolver;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TablesMustBeAvailable extends BaseRule {
    private final List<Table> tables;

    private final LocalDate dateCheck;

    private final ReservationByDateResolver reservationByDateResolver;

    private final Set<String> invalidTableNames;

    public TablesMustBeAvailable(List<Table> tables, LocalDate dateCheck, ReservationByDateResolver reservationByDateResolver) {
        this.tables = tables;
        this.dateCheck = dateCheck;
        this.reservationByDateResolver = reservationByDateResolver;
        this.invalidTableNames = new HashSet<>();
    }

    @Override
    public String getValidateField() {
        return "table";
    }

    @Override
    public boolean isValid() {
        LocalDate date = dateCheck != null ? dateCheck : LocalDate.now();
        return tables.stream().allMatch((table) -> {
            if (!new TableMustBeAvailable(table, date, reservationByDateResolver).isValid()) {
                this.invalidTableNames.add(table.getTableNumber());
                return false;
            }
            return true;
        });
    }

    @Override
    public String getMessage() {
        return String.join(",", invalidTableNames) + " đã được đặt và chưa hoàn thành vào ngày này, vui lòng chọn bàn khác!";
    }
}
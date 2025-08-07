package com.laklu.pos.validator;

import com.laklu.pos.entities.ReservationTable;
import com.laklu.pos.entities.Table;
import com.laklu.pos.services.ReservationByDateResolver;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
public class TableMustBeAvailable extends BaseRule {
    private final Table table;

    private final LocalDate dateCheck;

    private ReservationByDateResolver reservationByDateResolver;

    @Override
    public String getValidateField() {
        return "table";
    }

    @Override
    public boolean isValid() {
        LocalDate now = dateCheck != null ? dateCheck : LocalDate.now();
        List<ReservationTable> reservationTables = reservationByDateResolver.resolveReservationsDate(now, List.of(table));
        return reservationTables.isEmpty();
    }

    @Override
    public String getMessage() {
        return "Bàn đã được đặt và chưa hoàn thành vào ngày này, vui lòng chọn bàn khác!";
    }
}
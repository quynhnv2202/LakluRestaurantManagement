package com.laklu.pos.validator;

import com.laklu.pos.entities.Table;
import com.laklu.pos.enums.StatusTable;
import com.laklu.pos.repositories.ReservationTableRepository;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
public class TableMustAvailable extends BaseRule {

    private final List<Table> tables;
    private final ReservationTableRepository reservationTableRepository;
    private final LocalDate checkinDate;

    @Override
    public String getValidateField() {
        return "tableStatus";
    }

    @Override
    public boolean isValid() {
        return tables.stream().allMatch(table ->
                table.getStatus() == StatusTable.AVAILABLE ||
                        reservationTableRepository.countByTableAndDateAndNotCompleted(table.getId(), checkinDate) == 0
        );
    }

    @Override
    public String getMessage() {
        return "Bàn đã được đặt và chưa hoàn thành vào ngày này, vui lòng chọn bàn khác!";
    }
}

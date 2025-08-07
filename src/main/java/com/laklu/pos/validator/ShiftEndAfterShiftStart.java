package com.laklu.pos.validator;

import com.laklu.pos.dataObjects.request.NewSchedule;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ShiftEndAfterShiftStart extends BaseRule {
    private final NewSchedule schedule;

    @Override
    public String getValidateField() {
        return "shiftEnd";
    }

    @Override
    public boolean isValid() {
        if (schedule.getShiftStart() == null || schedule.getShiftEnd() == null) {
            return true; // Để các @NotNull xử lý trường hợp null
        }
        return schedule.getShiftEnd().isAfter(schedule.getShiftStart());
    }

    @Override
    public String getMessage() {
        return "Thời gian kết thúc ca phải sau thời gian bắt đầu ca";
    }
}


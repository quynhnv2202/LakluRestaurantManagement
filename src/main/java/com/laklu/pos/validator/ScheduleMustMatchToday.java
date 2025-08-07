package com.laklu.pos.validator;

import com.laklu.pos.entities.Schedule;
import com.laklu.pos.services.ScheduleService;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
public class ScheduleMustMatchToday extends BaseRule {

    private final Schedule schedule;
    private final ScheduleService scheduleService;

    @Override
    public String getValidateField() {
        return "scheduleDate";
    }

    @Override
    public boolean isValid() {
        LocalDate today = LocalDate.now();
        return schedule.getShiftStart().toLocalDate().equals(today);
    }

    @Override
    public String getMessage() {
        return "Lịch làm việc không khớp với ngày hiện tại, không thể tạo mã QR.";
    }
}

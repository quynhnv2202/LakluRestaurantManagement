package com.laklu.pos.validator;

import com.laklu.pos.dataObjects.request.NewSchedule;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class ScheduleTimeMustBePresentOrFuture extends BaseRule {
    private final NewSchedule schedule;

    @Override
    public String getValidateField() {
        return "shiftStart";
    }

    @Override
    public boolean isValid() {
        if (schedule.getShiftStart() == null) {
            return true; // Để @NotNull xử lý trường hợp null
        }

        LocalDateTime now = LocalDateTime.now();
        // Kiểm tra thời gian bắt đầu phải là hiện tại hoặc tương lai
        return !schedule.getShiftStart().isBefore(now);
    }

    @Override
    public String getMessage() {
        return "Thời gian bắt đầu ca làm việc phải là hiện tại hoặc tương lai";
    }
} 
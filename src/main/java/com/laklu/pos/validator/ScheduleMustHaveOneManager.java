package com.laklu.pos.validator;

import com.laklu.pos.dataObjects.request.NewSchedule;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ScheduleMustHaveOneManager extends BaseRule {

    private final NewSchedule schedule;

    @Override
    public String getValidateField() {
        return "manager";
    }

    @Override
    public boolean isValid() {
        long managerCount = schedule.getUser().stream()
                .filter(userDTO -> userDTO.getIsManager())
                .count();
        return managerCount == 1;
    }

    @Override
    public String getMessage() {
        return "Phải có duy nhất một nhân viên là quản lý trong ca làm việc";
    }
} 
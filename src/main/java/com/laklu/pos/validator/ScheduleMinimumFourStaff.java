package com.laklu.pos.validator;

import com.laklu.pos.dataObjects.request.NewSchedule;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ScheduleMinimumFourStaff extends BaseRule {

    private final NewSchedule schedule;

    @Override
    public String getValidateField() {
        return "staff";
    }

    @Override
    public boolean isValid() {
        if (schedule.getUser() == null) {
            return false; // Nếu không có user nào, cũng không hợp lệ
        }
        
        // Kiểm tra số lượng nhân viên phải lớn hơn hoặc bằng 4
        return schedule.getUser().size() >= 5;
    }

    @Override
    public String getMessage() {
        return "Lịch làm việc phải có tối thiểu 5 người";
    }
} 
package com.laklu.pos.validator;

import com.laklu.pos.entities.Schedule;
import com.laklu.pos.entities.User;
import com.laklu.pos.repositories.ScheduleRepository;
import com.laklu.pos.services.ScheduleService;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Rule kiểm tra xem người dùng có lịch làm việc trong ngày hiện tại không 
 * và nếu có thì đã điểm danh chưa
 */
@AllArgsConstructor
public class UserMustHaveScheduleAndAttendance extends BaseRule {

    private final User user;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleService scheduleService;
    private final boolean checkAttendance; // true: kiểm tra điểm danh, false: chỉ kiểm tra lịch

    @Override
    public String getValidateField() {
        return "schedule";
    }

    @Override
    public boolean isValid() {
        // Tìm lịch làm việc hiện tại của người dùng
        Optional<Schedule> currentSchedule = scheduleRepository.findCurrentScheduleByStaffId(user.getId());
        
        // Nếu không tìm thấy lịch làm việc
        if (currentSchedule.isEmpty()) {
            setMessage("Bạn không có lịch làm việc trong thời gian hiện tại.");
            return false;
        }
        
        // Nếu không cần kiểm tra điểm danh
        if (!checkAttendance) {
            return true;
        }
        
        // Kiểm tra đã điểm danh chưa
        boolean hasCheckedIn = scheduleService.hasCheckedIn(currentSchedule.get(), user);
        if (!hasCheckedIn) {
            setMessage("Bạn chưa điểm danh cho ca làm việc hiện tại.");
            return false;
        }
        
        return true;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
} 
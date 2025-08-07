package com.laklu.pos.validator;

import com.laklu.pos.entities.Schedule;
import com.laklu.pos.repositories.AttendanceRepository;
import com.laklu.pos.repositories.CashRegisterRepository;
import lombok.AllArgsConstructor;

/**
 * Validator để kiểm tra xem lịch làm việc có thể xóa được hay không
 */
@AllArgsConstructor
public class ScheduleCanBeDeleted extends BaseRule {
    private final Schedule schedule;
    private final AttendanceRepository attendanceRepository;
    private final CashRegisterRepository cashRegisterRepository;
    
    @Override
    public String getValidateField() {
        return "schedule";
    }
    
    @Override
    public boolean isValid() {
        // Nếu lịch làm việc đã bắt đầu, không cho phép xóa
        if (schedule.getShiftStart().isBefore(java.time.LocalDateTime.now())) {
            setMessage("Không thể xóa lịch làm việc đã bắt đầu");
            return false;
        }
        
        // Kiểm tra xem có nhân viên đã điểm danh chưa
        boolean hasAttendances = !attendanceRepository.findAll().stream()
                .filter(attendance -> attendance.getSchedule() != null && 
                        attendance.getSchedule().getId().equals(schedule.getId()))
                .findAny()
                .isEmpty();
                
        if (hasAttendances) {
            setMessage("Không thể xóa lịch làm việc đã có nhân viên điểm danh");
            return false;
        }
        
        // Kiểm tra xem lịch làm việc có liên kết với CashRegister không
        boolean hasCashRegister = cashRegisterRepository.findByScheduleId(schedule.getId()).isPresent();
        if (hasCashRegister) {
            setMessage("Không thể xóa lịch làm việc đã liên kết với bảng thu chi");
            return false;
        }
        
        return true;
    }
    
    @Override
    public String getMessage() {
        return super.getMessage();
    }
} 
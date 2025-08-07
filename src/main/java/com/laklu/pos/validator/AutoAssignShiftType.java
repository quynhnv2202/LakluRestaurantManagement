package com.laklu.pos.validator;

import com.laklu.pos.dataObjects.request.NewSchedule;
import com.laklu.pos.enums.ShiftType;
import lombok.AllArgsConstructor;

import java.time.LocalTime;
import java.time.Duration;

@AllArgsConstructor
public class AutoAssignShiftType extends BaseRule {
    private final NewSchedule schedule;

    // Định nghĩa khoảng thời gian cho các ca
    private static final LocalTime MORNING_START = LocalTime.of(6, 0); 
    private static final LocalTime MORNING_END = LocalTime.of(12, 0);
    private static final LocalTime EVENING_START = LocalTime.of(12, 0);
    private static final LocalTime EVENING_END = LocalTime.of(18, 0);
    private static final LocalTime NIGHT_START = LocalTime.of(18, 0);
    private static final LocalTime NIGHT_END = LocalTime.of(6, 0); // Kết thúc vào sáng hôm sau
    private static final LocalTime LATE_NIGHT = LocalTime.of(0, 0); // Nửa đêm

    @Override
    public String getValidateField() {
        return "shiftType";
    }

    @Override
    public boolean isValid() {
        if (schedule.getShiftStart() == null) {
            return true; // Để @NotNull xử lý trường hợp null
        }

        // Nếu người dùng không chọn shiftType, tự động gán dựa trên thời gian bắt đầu và kết thúc
        if (schedule.getShiftType() == null) {
            ShiftType assignedType = determineShiftType(
                schedule.getShiftStart().toLocalTime(), 
                schedule.getShiftEnd() != null ? schedule.getShiftEnd().toLocalTime() : null
            );
            schedule.setShiftType(assignedType);
        }

        return true; // Luôn trả về true vì đây chỉ là tự động gán giá trị
    }

    private ShiftType determineShiftType(LocalTime startTime, LocalTime endTime) {
        // Nếu không có thời gian kết thúc, chỉ dựa vào thời gian bắt đầu để xác định ca
        if (endTime == null) {
            return determineShiftTypeByStartTime(startTime);
        }

        // Dựa vào cả thời gian bắt đầu và kết thúc để xác định ca
        
        // Ca sáng đến chiều (6:00-18:00)
        if (isTimeInRange(startTime, MORNING_START, MORNING_END) && 
            isTimeInRange(endTime, EVENING_START, EVENING_END)) {
            return ShiftType.MORNING_TO_EVENING;
        }
        
        // Ca chiều đến tối (12:00-0:00)
        if (isTimeInRange(startTime, EVENING_START, EVENING_END) && 
            (isTimeInRange(endTime, NIGHT_START, LocalTime.of(23, 59)) || 
             isTimeInRange(endTime, LocalTime.of(0, 0), MORNING_START))) {
            return ShiftType.EVENING_TO_NIGHT;
        }
        
        // Ca cả ngày (6:00-0:00)
        if (isTimeInRange(startTime, MORNING_START, MORNING_END) && 
            (isTimeInRange(endTime, NIGHT_START, LocalTime.of(23, 59)) || 
             isTimeInRange(endTime, LocalTime.of(0, 0), MORNING_START))) {
            return ShiftType.FULL_DAY;
        }
        
        // Nếu không phù hợp với ca kéo dài, sử dụng thời gian bắt đầu để xác định ca tiêu chuẩn
        return determineShiftTypeByStartTime(startTime);
    }

    private ShiftType determineShiftTypeByStartTime(LocalTime startTime) {
        if (isTimeInRange(startTime, MORNING_START, MORNING_END)) {
            return ShiftType.MORNING;
        } else if (isTimeInRange(startTime, EVENING_START, EVENING_END)) {
            return ShiftType.EVENING;
        } else {
            return ShiftType.NIGHT;
        }
    }

    private boolean isTimeInRange(LocalTime time, LocalTime start, LocalTime end) {
        // Xử lý trường hợp khoảng thời gian bắt đầu >= kết thúc (xuyên qua nửa đêm)
        if (start.isAfter(end) || start.equals(end)) {
            return !time.isAfter(end) || !time.isBefore(start);
        }
        // Khoảng thời gian bình thường (bắt đầu < kết thúc)
        return !time.isBefore(start) && !time.isAfter(end);
    }

    @Override
    public String getMessage() {
        return "Tự động gán loại ca dựa trên thời gian làm việc";
    }
} 
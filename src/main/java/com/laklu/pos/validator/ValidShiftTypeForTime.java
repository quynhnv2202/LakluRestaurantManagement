package com.laklu.pos.validator;

import com.laklu.pos.dataObjects.request.NewSchedule;
import com.laklu.pos.enums.ShiftType;
import lombok.AllArgsConstructor;

import java.time.LocalTime;
import java.time.Duration;

@AllArgsConstructor
public class ValidShiftTypeForTime extends BaseRule {
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
        if (schedule.getShiftStart() == null || schedule.getShiftEnd() == null || schedule.getShiftType() == null) {
            return true; // Để các @NotNull xử lý trường hợp null
        }

        LocalTime startTime = schedule.getShiftStart().toLocalTime();
        LocalTime endTime = schedule.getShiftEnd().toLocalTime();
        ShiftType shiftType = schedule.getShiftType();

        switch (shiftType) {
            case MORNING:
                // Ca sáng: bắt đầu từ 6:00 đến 12:00
                return isTimeInRange(startTime, MORNING_START, MORNING_END);
                
            case EVENING:
                // Ca chiều: bắt đầu từ 12:00 đến 18:00
                return isTimeInRange(startTime, EVENING_START, EVENING_END);
                
            case NIGHT:
                // Ca tối: bắt đầu từ 18:00 đến 6:00 sáng hôm sau
                return isTimeInRange(startTime, NIGHT_START, LocalTime.of(23, 59)) || 
                       isTimeInRange(startTime, LocalTime.of(0, 0), NIGHT_END);
                       
            case MORNING_TO_EVENING:
                // Ca sáng đến chiều: bắt đầu từ 6:00 đến 12:00, kết thúc từ 12:00 đến 18:00
                return isTimeInRange(startTime, MORNING_START, MORNING_END) && 
                       (endTime.isAfter(EVENING_START) && !endTime.isAfter(EVENING_END));
                       
            case EVENING_TO_NIGHT:
                // Ca chiều đến tối: bắt đầu từ 12:00 đến 18:00, kết thúc sau 18:00
                return isTimeInRange(startTime, EVENING_START, EVENING_END) && 
                       (endTime.isAfter(NIGHT_START) || endTime.isBefore(MORNING_START));
                       
            case FULL_DAY:
                // Ca cả ngày: bắt đầu từ sáng, kết thúc vào tối hoặc đêm
                return isTimeInRange(startTime, MORNING_START, MORNING_END) && 
                       (endTime.isAfter(NIGHT_START) || endTime.isBefore(MORNING_START));
                
            default:
                return false;
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
        return "Loại ca không phù hợp với thời gian làm việc. Ca sáng: 6:00-12:00, Ca chiều: 12:00-18:00, Ca tối: 18:00-6:00";
    }
} 
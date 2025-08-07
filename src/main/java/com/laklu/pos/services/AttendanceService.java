package com.laklu.pos.services;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.dataObjects.request.UpdateAttendanceDTO;
import com.laklu.pos.dataObjects.response.AttendanceStatsDTO;
import com.laklu.pos.entities.Attendance;
import com.laklu.pos.entities.User;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.mapper.AttendanceMapper;
import com.laklu.pos.repositories.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;
    private final AttendanceLogService attendanceLogService;

    public Attendance findOrFail(Integer id) {
        return attendanceRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    @Transactional
    public Attendance updateAttendance(Attendance attendance, UpdateAttendanceDTO updateAttendanceDTO) {
        // Lưu trạng thái cũ trước khi cập nhật
        Attendance.Status oldStatus = attendance.getStatus();
        LocalTime oldClockIn = attendance.getClockIn();
        LocalTime oldClockOut = attendance.getClockOut();
        String oldNote = attendance.getNote();

        // Cập nhật thông tin mới
        attendanceMapper.updateAttendanceFromDto(updateAttendanceDTO, attendance);

        // Lưu attendance đã cập nhật
        attendance = attendanceRepository.save(attendance);

        // Ghi log thay đổi
        attendanceLogService.logAttendanceUpdate(attendance, oldStatus, oldClockIn, oldClockOut, oldNote);

        return attendance;
    }

    public AttendanceStatsDTO getAttendanceStats(User staff, String salaryMonth) {
        List<Attendance> attendances = attendanceRepository.findByStaffIdAndSalaryMonth(staff.getId(), salaryMonth);
        return calculateAttendanceStats(attendances, staff.getId());
    }

    public Map<Integer, AttendanceStatsDTO> getAttendanceStatsForAllStaff(String salaryMonth) {
        List<Attendance> attendances = attendanceRepository.countAttendanceForAllStaff(salaryMonth);
        return attendances.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getStaff().getId(),
                        Collectors.collectingAndThen(Collectors.toList(), list -> calculateAttendanceStats(list, list.get(0).getStaff().getId()))
                ));
    }

    public double getTotalWorkingHours(Integer staffId, String salaryMonth) {
        List<Attendance> attendances = attendanceRepository.findByStaffIdAndSalaryMonth(staffId, salaryMonth);
        double totalWorkingHours = attendances.stream()
                .filter(a -> a.getClockIn() != null && a.getClockOut() !=null)
                .mapToDouble(a -> calculateWorkingHours(a.getClockIn(), a.getClockOut()))
                .sum();
        return Math.round(totalWorkingHours * 100.0) / 100.0;
    }

    private double calculateWorkingHours(LocalTime clockIn, LocalTime clockOut) {
        // Nếu clockOut lớn hơn hoặc bằng clockIn, tính trực tiếp
        if (!clockOut.isBefore(clockIn)) {
            return Duration.between(clockIn, clockOut).toMinutes() / 60.0;
        }

        // Nếu clockOut nhỏ hơn clockIn, giả định clockOut thuộc ngày hôm sau
        // Tính thời gian từ clockIn đến nửa đêm
        LocalTime midnight = LocalTime.of(23, 59, 59, 999999999);
        long minutesToMidnight = Duration.between(clockIn, midnight).toMinutes();

        // Tính thời gian từ nửa đêm đến clockOut
        long minutesFromMidnight = Duration.between(LocalTime.MIDNIGHT, clockOut).toMinutes();

        // Tổng thời gian làm việc (phút) = thời gian đến nửa đêm + thời gian từ nửa đêm
        long totalMinutes = minutesToMidnight + minutesFromMidnight + 1; // +1 để tính cả giây cuối của ngày

        return totalMinutes / 60.0; // Chuyển sang giờ
    }

    private AttendanceStatsDTO calculateAttendanceStats(List<Attendance> attendances, Integer staffId) {
        int totalWorkingDays = attendances.size();
        final long GRACE_PERIOD_MINUTES = 10;

        List<AttendanceTimePair> timePairs = attendances.stream()
                .filter(a -> a.getClockIn() != null && a.getSchedule() != null && a.getSchedule().getShiftStart() != null)
                .map(a -> new AttendanceTimePair(
                        a.getSchedule().getShiftStart(),
                        a.getAttendanceDate().atTime(a.getClockIn())
                ))
                .collect(Collectors.toList());

        int lateCount = (int) timePairs.stream()
                .filter(pair ->{
                    long minutesLate = java.time.Duration.between(pair.shiftStart, pair.clockIn).toMinutes();
                    return minutesLate > GRACE_PERIOD_MINUTES;
                })
                .count();

        double lateHours = timePairs.stream()
                .filter(pair -> {
                    long minutesLate = java.time.Duration.between(pair.shiftStart, pair.clockIn).toMinutes();
                    return minutesLate > GRACE_PERIOD_MINUTES;
                })
                .mapToDouble(pair -> {
                    long minutesLate = java.time.Duration.between(pair.shiftStart, pair.clockIn).toMinutes();
                    return (minutesLate - GRACE_PERIOD_MINUTES) / 60.0;
                })
                .sum();
        lateHours = Math.round(lateHours * 100.0) / 100.0;

        return new AttendanceStatsDTO(staffId, totalWorkingDays, lateCount, lateHours);
    }

    private static class AttendanceTimePair {
        final LocalDateTime shiftStart;
        final LocalDateTime clockIn;

        AttendanceTimePair(LocalDateTime shiftStart, LocalDateTime clockIn) {
            this.shiftStart = shiftStart;
            this.clockIn = clockIn;
        }
    }
} 
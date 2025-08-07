package com.laklu.pos.services;

import com.laklu.pos.entities.ActivityLog;
import com.laklu.pos.entities.Attendance;
import com.laklu.pos.enums.TrackedResourceType;
import com.laklu.pos.repositories.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AttendanceLogService {
    private final ActivityLogService activityLogService;

    public void logAttendanceUpdate(Attendance attendance, 
                                  Attendance.Status oldStatus,
                                  LocalTime oldClockIn,
                                  LocalTime oldClockOut,
                                  String oldNote) {
        StringBuilder details = new StringBuilder("Cập nhật điểm danh: ");
        
        // Log thay đổi trạng thái
        if (oldStatus != attendance.getStatus()) {
            details.append(String.format("Trạng thái từ %s thành %s; ", oldStatus, attendance.getStatus()));
        }
        
        // Log thay đổi giờ check in
        if ((oldClockIn == null && attendance.getClockIn() != null) || 
            (oldClockIn != null && !oldClockIn.equals(attendance.getClockIn()))) {
            details.append(String.format("Giờ check-in từ %s thành %s; ", 
                oldClockIn != null ? oldClockIn.toString() : "chưa có", 
                attendance.getClockIn() != null ? attendance.getClockIn().toString() : "chưa có"));
        }
        
        // Log thay đổi giờ check out
        if ((oldClockOut == null && attendance.getClockOut() != null) || 
            (oldClockOut != null && !oldClockOut.equals(attendance.getClockOut()))) {
            details.append(String.format("Giờ check-out từ %s thành %s; ", 
                oldClockOut != null ? oldClockOut.toString() : "chưa có", 
                attendance.getClockOut() != null ? attendance.getClockOut().toString() : "chưa có"));
        }
        
        // Log thay đổi ghi chú
        if ((oldNote == null && attendance.getNote() != null) || 
            (oldNote != null && !oldNote.equals(attendance.getNote()))) {
            details.append(String.format("Ghi chú từ '%s' thành '%s'; ", 
                oldNote != null ? oldNote : "không có", 
                attendance.getNote() != null ? attendance.getNote() : "không có"));
        }

        activityLogService.logActivity(
            attendance,
            TrackedResourceType.Action.UPDATE,
            attendance.getId().toString(),
            TrackedResourceType.ATTENDANCE,
            details.toString()
        );
    }
} 
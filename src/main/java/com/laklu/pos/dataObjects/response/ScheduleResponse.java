package com.laklu.pos.dataObjects.response;

import com.laklu.pos.entities.Schedule;
import com.laklu.pos.entities.User;
import com.laklu.pos.enums.ShiftType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class ScheduleResponse {

    private Long id;
    // nhân viên được đăng kí
    private List<UserInfoResponse> staffs;
    // lượt checkin checkout thực tế
    private List<AttendanceResponse> attendances = new ArrayList<>();
    private LocalDateTime shiftStart;
    private LocalDateTime shiftEnd;
    private ShiftType shiftType;
    private String note;

    public ScheduleResponse(Schedule schedule) {
        this.id = schedule.getId();
        this.staffs = schedule.getStaffs().stream().map(UserInfoResponse::new).toList();
        this.attendances = schedule.getAttendances().stream().map(AttendanceResponse::new).toList();
        this.shiftStart = schedule.getShiftStart();
        this.shiftEnd = schedule.getShiftEnd();
        this.shiftType = schedule.getShiftType();
        this.note = schedule.getNote();
    }
}
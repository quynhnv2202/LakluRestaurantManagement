package com.laklu.pos.dataObjects.response;

import com.laklu.pos.entities.Attendance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceResponse {
    private Integer id;
    private Attendance.Status status;
    private String note;
    private LocalTime clockIn;
    private LocalTime clockOut;
    private UserInfoResponse userInfoResponse;

    public AttendanceResponse(Attendance attendance) {
        this.id = attendance.getId();
        this.status = attendance.getStatus();
        this.note = attendance.getNote();
        this.clockIn = attendance.getClockIn();
        this.clockOut = attendance.getClockOut();
        this.userInfoResponse = new UserInfoResponse(attendance.getStaff());
    }
}

package com.laklu.pos.dataObjects.request;

import com.laklu.pos.entities.Attendance;
import lombok.Data;

import java.time.LocalTime;

@Data
public class UpdateAttendanceDTO {
    private Attendance.Status status;
    private LocalTime clockIn;
    private LocalTime clockOut;
    private String note;
} 
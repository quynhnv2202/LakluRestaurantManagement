package com.laklu.pos.dataObjects.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.laklu.pos.dataObjects.response.Calendarable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleDetailDTO implements Calendarable<Long, ScheduleDetailDTO> {
    private Long id;
    private String managerFullName;
    private int numberOfStaff;
    private List<String> userFullNames;
    private String note;
    private LocalDateTime timeIn;
    private LocalDateTime timeOut;
    private String attended;
    private Map<String, Boolean> userAttendancesByFullName;
    private Map<String, ClockInOutInfo> userClockInClockOut;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClockInOutInfo {
        private LocalTime clockIn;
        private LocalTime clockOut;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public LocalDateTime getTimeIn() {
        return timeIn;
    }

    @Override
    public LocalDateTime getTimeOut() {
        return timeOut;
    }

    @Override
    @JsonIgnore
    public ScheduleDetailDTO getDetail() {
        return this;
    }
}
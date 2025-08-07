package com.laklu.pos.dataObjects.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttendanceStatsDTO {
    private Integer staffId;
    private Integer totalWorkingDays;
    private Integer lateCount;
    private Double lateHours;
}

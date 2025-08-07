package com.laklu.pos.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public enum EmploymentStatus {
    WORKING("Đang làm", "Nhân viên đang làm việc"),
    TEMPORARY_LEAVE("Tạm nghỉ", "Nhân viên đang tạm thời nghỉ"),
    RESIGNED("Đã nghỉ", "Nhân viên không còn làm việc");

    EmploymentStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    String label;
    String description;
}

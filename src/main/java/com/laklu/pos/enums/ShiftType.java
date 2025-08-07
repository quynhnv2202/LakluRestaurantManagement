package com.laklu.pos.enums;

public enum ShiftType {
    MORNING,     // Ca sáng (6:00 - 12:00)
    EVENING,     // Ca chiều (12:00 - 18:00)
    NIGHT,       // Ca tối (18:00 - 6:00)
    MORNING_TO_EVENING, // Ca sáng đến chiều (6:00 - 18:00)
    EVENING_TO_NIGHT,   // Ca chiều đến tối (12:00 - 0:00)
    FULL_DAY            // Ca cả ngày (6:00 - 0:00)
}

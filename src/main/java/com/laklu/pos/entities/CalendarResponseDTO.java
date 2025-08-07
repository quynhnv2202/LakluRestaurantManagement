package com.laklu.pos.entities;

import com.laklu.pos.dataObjects.response.Calendarable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CalendarResponseDTO<T, D> {
    T id;
    LocalDateTime timeIn;
    LocalDateTime timeOut;
    D detail;

    public CalendarResponseDTO(Calendarable<T, D> calendarable) {
        this.id = calendarable.getId();
        this.timeIn = calendarable.getTimeIn();
        this.timeOut = calendarable.getTimeOut();
        this.detail = calendarable.getDetail();
    }
}

package com.laklu.pos.dataObjects.response;

import java.time.LocalDateTime;

public interface Calendarable<T, D> {
   T getId();
   LocalDateTime getTimeIn();
   LocalDateTime getTimeOut();
   D getDetail();
}

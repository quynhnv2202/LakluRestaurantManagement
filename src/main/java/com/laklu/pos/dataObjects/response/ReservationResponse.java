package com.laklu.pos.dataObjects.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.laklu.pos.entities.Reservation;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReservationResponse implements Calendarable<Integer , ReservationResponse>{
    Integer id;
    String customerName;
    String customerPhone;
    LocalDateTime reservationTime;
    Reservation.Status status;
    String createBy;
    Integer numberOfPeople;
    LocalDateTime checkIn;
    LocalDateTime checkOut;
    List<TableInfo> tables;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public LocalDateTime getTimeIn() {
        return checkIn;
    }

    @Override
    public LocalDateTime getTimeOut() {
        return checkOut;
    }

    @Override
    @JsonIgnore
    public ReservationResponse getDetail() {
        return this;
    }
}
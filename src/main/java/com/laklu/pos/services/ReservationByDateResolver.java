package com.laklu.pos.services;

import com.laklu.pos.entities.ReservationTable;
import com.laklu.pos.entities.Table;

import java.time.LocalDate;
import java.util.List;

@FunctionalInterface
public interface ReservationByDateResolver {
    List<ReservationTable> resolveReservationsDate(LocalDate date, List<Table> tables);
}
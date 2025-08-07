package com.laklu.pos.validator;

import com.laklu.pos.entities.Reservation;
import com.laklu.pos.exceptions.httpExceptions.BadRequestException;
import com.laklu.pos.uiltis.Ultis;

import java.util.List;

public class ReservationMustHaveAtLeastOneTable implements Rule {
    private final Reservation reservation;
    private final List<Integer> tableIdsToDelete;

    public ReservationMustHaveAtLeastOneTable(Reservation reservation, List<Integer> tableIdsToDelete) {
        this.reservation = reservation;
        this.tableIdsToDelete = tableIdsToDelete;
    }

    @Override
    public String field() {
        return "tables";
    }

    @Override
    public boolean isValid() {
        int currentTableCount = reservation.getReservationTables().size();
        int tablesToDeleteCount = tableIdsToDelete.size();
        return currentTableCount > tablesToDeleteCount;
    }

    @Override
    public String getMessage() {
        return "Không thể xóa tất cả bàn. Đặt bàn phải có ít nhất 1 bàn.";
    }
} 
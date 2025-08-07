package com.laklu.pos.repositories;

import com.laklu.pos.entities.Reservation;
import com.laklu.pos.entities.ReservationTable;
import com.laklu.pos.entities.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationTableRepository extends JpaRepository<ReservationTable, Integer> {
    List<ReservationTable> findByReservation(Reservation reservation);

    @Query("SELECT COUNT(rt) FROM ReservationTable rt " +
            "WHERE rt.table.id = :tableId " +
            "AND FUNCTION('DATE', rt.reservation.reservationTime) = :date")
    long countByTableAndDate(@Param("tableId") Integer tableId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(rt) FROM ReservationTable rt " +
            "WHERE rt.table.id = :tableId " +
            "AND FUNCTION('DATE', rt.reservation.checkIn) = :date " +
            "AND rt.reservation.status <> 'COMPLETED'")
    long countByTableAndDateAndNotCompleted(@Param("tableId") Integer tableId, @Param("date") LocalDate date);

    @Query("SELECT rt FROM ReservationTable rt " +
            "WHERE FUNCTION('DATE', rt.reservation.checkIn) = :localDate " +
            "AND rt.table IN :tables " +
            "AND rt.reservation.status NOT IN ('CANCELLED', 'COMPLETED')")
    List<ReservationTable> findReservationsDate(LocalDate localDate, List<Table> tables);

    @Query("SELECT rt FROM ReservationTable rt " +
            "WHERE rt.reservation = :reservation " +
            "AND rt.table.id IN :tableIds")
    List<ReservationTable> findByReservationAndTables(@Param("reservation") Reservation reservation, @Param("tableIds") List<Integer> tableIds);

    @Modifying
    @Query(value = "DELETE FROM reservation_table WHERE reservation_id = :reservationId AND table_id IN :tableIds", nativeQuery = true)
    int deleteByReservationIdAndTableIds(@Param("reservationId") Integer reservationId, @Param("tableIds") List<Integer> tableIds);

    /**
     * Kiểm tra xem các bàn có đang được sử dụng trong đặt bàn khác nào không vào ngày cụ thể
     * 
     * @param tableIds Danh sách ID của các bàn cần kiểm tra
     * @param excludeReservationId ID của đặt bàn cần loại trừ khỏi việc kiểm tra (đặt bàn hiện tại)
     * @param date Ngày cần kiểm tra
     * @return true nếu có ít nhất một bàn đang được sử dụng vào ngày đó, false nếu không có bàn nào đang được sử dụng
     */
    @Query("SELECT COUNT(rt) > 0 FROM ReservationTable rt " +
           "WHERE rt.table.id IN :tableIds " +
           "AND rt.reservation.id != :excludeReservationId " +
           "AND rt.reservation.status NOT IN (com.laklu.pos.entities.Reservation$Status.CANCELLED, com.laklu.pos.entities.Reservation$Status.COMPLETED) " +
           "AND FUNCTION('DATE', rt.reservation.checkIn) = :date")
    boolean areTablesInUseByOtherReservationOnDate(
            @Param("tableIds") List<Integer> tableIds,
            @Param("excludeReservationId") Integer excludeReservationId,
            @Param("date") LocalDate date);
}


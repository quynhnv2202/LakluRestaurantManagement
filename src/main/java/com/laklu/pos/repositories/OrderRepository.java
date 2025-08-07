package com.laklu.pos.repositories;

import com.laklu.pos.entities.Order;
import com.laklu.pos.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {
    @Query(value = "SELECT * FROM orders WHERE reservation_id = :reservationId AND status NOT IN ('CANCELLED', 'COMPLETED', 'DELIVERED')", nativeQuery = true)
    List<Order> findByReservationIdAndStatusNotCancelledOrCompleted(@Param("reservationId") Integer reservationId);

    List<Order> findByReservation(Reservation reservation);
}

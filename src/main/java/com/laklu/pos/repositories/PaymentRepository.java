package com.laklu.pos.repositories;

import com.laklu.pos.dataObjects.response.DailyRevenueResponse;
import com.laklu.pos.entities.Order;
import com.laklu.pos.entities.Payment;
import com.laklu.pos.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByCode(String code);
    boolean existsById(Integer id);

    @Query("SELECT SUM(p.amountPaid) FROM Payment p WHERE p.paymentStatus = 'PAID' " +
            "AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalRevenueByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    List<Payment> findByPaymentStatusAndPaymentDateBetween(
            PaymentStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    Page<Payment> findPaymentByDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    Page<Payment> findAll(Pageable pageable);

    List<Payment> getPaymentByOrder(Order orderId);
    List<Payment> findByPaymentStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime createdAt);
}

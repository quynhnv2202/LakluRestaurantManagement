package com.laklu.pos.repositories;

import com.laklu.pos.entities.PaymentHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Integer> {
    Optional<PaymentHistory> findByPayment_Id(Integer paymentId);
    
    @Query("SELECT ph FROM PaymentHistory ph WHERE ph.transactionDate >= :startDate AND ph.transactionDate <= :endDate")
    List<PaymentHistory> findAllByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT ph FROM PaymentHistory ph WHERE ph.transactionDate >= :startDate AND ph.transactionDate <= :endDate")
    Page<PaymentHistory> findPageByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
}

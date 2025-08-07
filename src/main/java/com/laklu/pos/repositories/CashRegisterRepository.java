package com.laklu.pos.repositories;

import com.laklu.pos.entities.CashRegister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashRegisterRepository extends JpaRepository<CashRegister, Integer> {
    Optional<CashRegister> findByUserIdAndShiftEndIsNull(Integer userId);
    Optional<CashRegister> findByScheduleId(Long scheduleId);
    
    @Query("SELECT cr FROM CashRegister cr WHERE DATE(cr.shiftStart) = :date")
    List<CashRegister> findAllByDate(LocalDate date);
    
    @Query("SELECT cr FROM CashRegister cr WHERE DATE(cr.createdAt) >= :startDate AND DATE(cr.createdAt) <= :endDate")
    Page<CashRegister> findAllByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);
} 
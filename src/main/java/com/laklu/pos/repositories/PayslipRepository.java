
package com.laklu.pos.repositories;

import com.laklu.pos.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Integer> {
    Optional<Payslip> findByStaffAndSalaryMonth(User staff, String salaryMonth);
    List<Payslip> findAllBySalaryMonth(String salaryMonth);
    void deleteAllBySalaryMonth(String salaryMonth);
}
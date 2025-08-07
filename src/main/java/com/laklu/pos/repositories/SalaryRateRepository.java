package com.laklu.pos.repositories;

import com.laklu.pos.entities.SalaryRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalaryRateRepository extends JpaRepository<SalaryRate, Integer> {
    Optional<SalaryRate> findById(Integer id);

    Optional<SalaryRate> findByLevelName(String name);
}

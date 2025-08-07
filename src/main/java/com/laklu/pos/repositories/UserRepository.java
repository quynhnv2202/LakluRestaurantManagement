package com.laklu.pos.repositories;

import com.laklu.pos.entities.User;
import com.laklu.pos.entities.Role;
import com.laklu.pos.entities.SalaryRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> getUserByUsername(String username);

    Optional<User> findByUsername(String username);

    boolean existsByRolesContaining(Role role);

    boolean existsBySalaryRate(SalaryRate salaryRate);
}

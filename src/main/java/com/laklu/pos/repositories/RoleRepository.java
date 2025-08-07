package com.laklu.pos.repositories;

import com.laklu.pos.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findRoleById(Integer id);

    Optional<Role> findByName(String name);
}

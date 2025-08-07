package com.laklu.pos.repositories;

import com.laklu.pos.entities.Profile;
import com.laklu.pos.entities.User;
import com.laklu.pos.enums.EmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Integer> {
    Optional<Profile> findByUserId(Integer userId);
    Optional<Profile> findByUser(User user);
    List<Profile> findByUserIn(Set<User> users);
    List<Profile> findByEmploymentStatusNot(EmploymentStatus status);
}

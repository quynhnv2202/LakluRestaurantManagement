package com.laklu.pos.repositories;

import com.laklu.pos.entities.Schedule;
import com.laklu.pos.entities.ScheduleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleUserRepository extends JpaRepository<ScheduleUser, Long> {
    void deleteAllByScheduleId(Long scheduleId);
    List<ScheduleUser> findAllByScheduleId(Long scheduleId);
} 
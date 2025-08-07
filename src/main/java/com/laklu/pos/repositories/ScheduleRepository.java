package com.laklu.pos.repositories;

import com.laklu.pos.entities.Schedule;
import com.laklu.pos.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("SELECT s FROM Schedule s JOIN s.scheduleUsers su JOIN su.user u WHERE u.id = :staffId AND s.shiftStart <= CURRENT_TIMESTAMP AND s.shiftEnd >= CURRENT_TIMESTAMP")
    Optional<Schedule> findCurrentScheduleByStaffId(@Param("staffId") Integer staffId);

    List<Schedule> findAllByScheduleUsers_User(User user);

    List<Schedule> findByShiftStartBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT s FROM Schedule s JOIN s.scheduleUsers su JOIN su.user u WHERE u.id = :userId " +
            "AND NOT(s.shiftEnd < :startDate OR s.shiftStart > :endDate)")
    List<Schedule> findByUserIdAndDateRange(
            @Param("userId") Integer userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}

package com.laklu.pos.repositories;

import com.laklu.pos.dataObjects.response.AttendanceStatsDTO;
import com.laklu.pos.entities.Attendance;
import com.laklu.pos.entities.Schedule;
import com.laklu.pos.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    Optional<Attendance> findByScheduleAndStaff(Schedule schedule, User staff);

    @Query("SELECT a FROM Attendance a " +
            "WHERE a.staff.id = :staffId " +
            "AND FUNCTION('DATE_FORMAT', a.attendanceDate, '%Y-%m') = :salaryMonth")
    List<Attendance> findByStaffIdAndSalaryMonth(Integer staffId, String salaryMonth);

    @Query("SELECT a FROM Attendance a " +
            "WHERE FUNCTION('DATE_FORMAT', a.attendanceDate, '%Y-%m') = :salaryMonth")
    List<Attendance> countAttendanceForAllStaff(String salaryMonth);
}

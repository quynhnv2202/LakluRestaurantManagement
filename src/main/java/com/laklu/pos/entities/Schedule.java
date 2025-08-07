package com.laklu.pos.entities;

import com.laklu.pos.controllers.ActivityLogListener;
import com.laklu.pos.dataObjects.response.Calendarable;
import com.laklu.pos.dataObjects.response.ScheduleDetailDTO;
import com.laklu.pos.enums.ShiftType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(ActivityLogListener.class)
public class Schedule implements Identifiable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ScheduleUser> scheduleUsers = new ArrayList<>();// Khởi tạo danh sách ngay tại đây
  
    @Column(name = "shift_start", nullable = false)
    private LocalDateTime shiftStart; // Giờ bắt đầu ca làm việc

    @Column(name = "shift_end", nullable = false)
    private LocalDateTime shiftEnd; // Giờ kết thúc ca làm việc

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false)
    private ShiftType shiftType; // Loại ca làm việc (SÁNG, CHIỀU, TỐI)

    @Column(name = "note", columnDefinition = "TEXT")
    private String note; // Ghi chú (nếu có)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // Thời gian tạo lịch

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời gian cập nhật lịch

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)

    private List<Attendance> attendances = new ArrayList<>(); // Khởi tạo danh sách điểm danh


    @Override
    public Long getId() {
        return id;
    }


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public List<User> getStaffs() {
        return scheduleUsers.stream()
                .map(ScheduleUser::getUser)
                .collect(Collectors.toList());
    }
}
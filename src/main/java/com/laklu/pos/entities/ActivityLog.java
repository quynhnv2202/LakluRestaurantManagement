package com.laklu.pos.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import com.laklu.pos.enums.TrackedResourceType.Action;

@Data
@Entity
@jakarta.persistence.Table(name = "activity_logs")
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "staff_id")
    private Integer staffId;

    @Column(name = "action", nullable = false, length = 50) // Update length as needed
    @Enumerated(EnumType.STRING)
    private Action action;

    @Column(name = "target")
    private String target;

    @Column(name = "target_id")
    private String targetId;

    @Column(name = "details")
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
package com.laklu.pos.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.laklu.pos.controllers.ActivityLogListener;
import com.laklu.pos.enums.StatusTable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@EntityListeners(ActivityLogListener.class)
@jakarta.persistence.Table(name = "tables")
public class Table implements Identifiable<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "table_number", nullable = false, length = 50)
    String tableNumber;

    @Column(name = "capacity", nullable = false)
    Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    StatusTable status;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    LocalDateTime updatedAt;

    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    Set<ReservationTable> reservationTables;

    @Override
    public Integer getId() { // Trả về String thay vì Integer
        return id;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
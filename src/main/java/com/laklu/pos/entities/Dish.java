package com.laklu.pos.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Entity
@jakarta.persistence.Table(name = "dish")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dish implements Identifiable<Integer>, InteractWithAttachments<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double price;

    //cần chế biến sẽ là true
    @Column(name = "requires_preparation", nullable = false)
    private Boolean requiresPreparation = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuItem> menuItems;

    @ManyToMany
    @JoinTable(
            name = "dish_attachment",
            joinColumns = @JoinColumn(name = "dish_id"),
            inverseJoinColumns = @JoinColumn(name = "attachment_id")
    )
    private Set<Attachment> attachments;

    @Override
    public void addAttachment(Attachment... attachment) {
        attachments.addAll(Arrays.asList(attachment));
    }

    @Override
    public Integer getId() {
        return id;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}

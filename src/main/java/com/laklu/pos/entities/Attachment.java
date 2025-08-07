package com.laklu.pos.entities;

import com.laklu.pos.controllers.ActivityLogListener;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(ActivityLogListener.class)
public class Attachment implements Identifiable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "random_name", nullable = false, unique = true)
    private String randomName;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "target_name")
    private String targetName; // nullable

    @Column(name = "target_id")
    private String targetId; // nullable

    @Column(name = "path", nullable = false)
    private String path;

    @Override
    public Long getId() { // Trả về String thay vì Integer
        return id;
    }
}


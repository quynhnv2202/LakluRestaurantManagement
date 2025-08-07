package com.laklu.pos.entities;

import com.laklu.pos.enums.Department;
import com.laklu.pos.enums.EmploymentStatus;
import com.laklu.pos.enums.Gender;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Profile implements InteractWithAttachments<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
    User user; // Liên kết với bảng users

    @Column( name = "full_name")
    String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    Gender gender;

    @Column(name = "date_of_birth")
    LocalDateTime dateOfBirth;

    @Column( name = "phone_number", unique = true, length = 15)
    String phoneNumber;

    @Column( name = "address")
    String address;

    @Column(name = "avatar")
    String avatar; // Ảnh đại diện (URL hoặc đường dẫn lưu)

    @Enumerated(EnumType.STRING)
    @Column(name = "department")
    Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status")
    EmploymentStatus employmentStatus;

    @Column(name = "hire_date")
    LocalDateTime hireDate;

    @Column( name = "bank_account")
    String bankAccount;

    @Column( name = "bank_number", unique = true)
    String bankNumber;
    
    @ManyToMany
    @JoinTable(
            name = "profile_attachment",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "attachment_id")
    )
    Set<Attachment> attachments = new HashSet<>();
    
    @Override
    public Set<Attachment> getAttachments() {
        return attachments;
    }

    @Override
    public void addAttachment(Attachment... attachment) {
        if (attachments == null) {
            attachments = new HashSet<>();
        }
        attachments.addAll(Arrays.asList(attachment));
    }

    @Override
    public void setAttachments(Set<Attachment> attachments) {
        this.attachments = attachments;
    }

    @Override
    public Integer getId() {
        return id;
    }
}

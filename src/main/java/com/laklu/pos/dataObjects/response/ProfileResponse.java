package com.laklu.pos.dataObjects.response;

import com.laklu.pos.enums.Department;
import com.laklu.pos.enums.EmploymentStatus;
import com.laklu.pos.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    
    private Integer id;
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private Gender gender;
    private LocalDateTime dateOfBirth;
    private String phoneNumber;
    private String address;
    private String avatar;
    @JsonIgnore
    private Department department;
    private EmploymentStatus employmentStatus;
    private LocalDateTime hireDate;
    private String bankAccount;
    private String bankNumber;
    private List<PersistAttachmentResponse> avatarImages;

    @JsonProperty("department")
    public String getDepartmentLabel() {
        return department != null ? department.getLabel() : null;
    }
} 
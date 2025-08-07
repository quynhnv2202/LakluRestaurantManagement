package com.laklu.pos.dataObjects.response;

import com.laklu.pos.entities.Profile;
import com.laklu.pos.entities.User;
import com.laklu.pos.enums.Department;
import com.laklu.pos.enums.EmploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {
    private int id;
    private String fullName;
    private String email;
    private Department department;
    private EmploymentStatus employmentStatus;
    
    public UserDetailResponse(User user, Profile profile) {
        this.id = user.getId();
        this.email = user.getEmail();
        
        if (profile != null) {
            this.fullName = profile.getFullName();
            this.department = profile.getDepartment();
            this.employmentStatus = profile.getEmploymentStatus();
        }
    }
} 
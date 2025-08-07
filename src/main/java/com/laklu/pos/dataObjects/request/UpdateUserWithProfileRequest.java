package com.laklu.pos.dataObjects.request;

import com.laklu.pos.enums.Department;
import com.laklu.pos.enums.EmploymentStatus;
import com.laklu.pos.enums.Gender;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserWithProfileRequest {
    // Thông tin người dùng
    private String email;
    private String phone;
    private List<Integer> roleIds;
    private Integer salaryRateId;
    
    // Thông tin profile
    private String fullName;
    private Gender gender;
    
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDateTime dateOfBirth;
    
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;
    
    private String address;
    private Department department;
    private EmploymentStatus employmentStatus;
    private LocalDateTime hireDate;
    private String bankAccount;
    private String bankNumber;
} 
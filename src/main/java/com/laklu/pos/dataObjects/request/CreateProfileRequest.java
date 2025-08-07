package com.laklu.pos.dataObjects.request;

import com.laklu.pos.enums.Department;
import com.laklu.pos.enums.EmploymentStatus;
import com.laklu.pos.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProfileRequest {
    
    @NotNull(message = "User ID không được để trống")
    private Integer userId;
    
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;
    
    @NotNull(message = "Giới tính không được để trống")
    private Gender gender;
    
    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDateTime dateOfBirth;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;
    
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;
    
    private String avatar;
    
    @NotNull(message = "Phòng ban không được để trống")
    private Department department;
    
    @NotNull(message = "Trạng thái làm việc không được để trống")
    private EmploymentStatus employmentStatus;
    
    @NotNull(message = "Ngày bắt đầu làm việc không được để trống")
    private LocalDateTime hireDate;
    
    @NotBlank(message = "Tên ngân hàng không được để trống")
    private String bankAccount;
    
    @NotBlank(message = "Số tài khoản ngân hàng không được để trống")
    private String bankNumber;
} 
package com.laklu.pos.dataObjects.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.laklu.pos.entities.Profile;
import com.laklu.pos.entities.Role;
import com.laklu.pos.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserWithProfileResponse {
    private int id;
    private String username;
    private String email;
    private Set<String> roles;
    
    // Thông tin lương
    private String salaryRateName; // Tên mức lương
    private BigDecimal salaryAmount; // Số tiền lương
    private String salaryType; // Loại lương (theo giờ, theo tháng)
    
    @JsonProperty("profile")
    private ProfileResponse profile;
    
    public static UserWithProfileResponse fromUserAndProfile(User user, ProfileResponse profile) {
        return UserWithProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .salaryRateName(user.getSalaryRate() != null ? user.getSalaryRate().getLevelName() : null)
                .salaryAmount(user.getSalaryRate() != null ? user.getSalaryRate().getAmount() : null)
                .salaryType(user.getSalaryRate() != null ? user.getSalaryRate().getType().toString() : null)
                .profile(profile)
                .build();
    }
} 
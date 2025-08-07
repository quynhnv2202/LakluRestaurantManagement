package com.laklu.pos.dataObjects.response;

import com.laklu.pos.entities.Role;
import com.laklu.pos.valueObjects.UserPrincipal;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class AuthUserResponse {
    private int id;
    private String username;
    private String email;
    private List<String> permissions;
    private String nameSalary;
    private List<String> roleNames;

    public AuthUserResponse(UserPrincipal userPrincipal) {
        this.id = userPrincipal.getPersitentUser().getId();
        this.username = userPrincipal.getPersitentUser().getUsername();
        this.email = userPrincipal.getPersitentUser().getEmail();
        this.permissions = userPrincipal.pluckPermissionAlias();
        
        // Lấy danh sách tên các vai trò
        Set<Role> roles = userPrincipal.getPersitentUser().getRoles();
        this.roleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        
        if (userPrincipal.getPersitentUser().getSalaryRate() != null) {
            this.nameSalary = userPrincipal.getPersitentUser().getSalaryRate().getLevelName();
        } else {
            this.nameSalary = null; // or handle the null case as needed
        }
    }
}
package com.laklu.pos.dataObjects.response;

import com.laklu.pos.entities.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class UserResponse {
    private int id;
    private String username;
    private String email;
    private Set<String> roles;
    private String nameSalaryRate;

    public UserResponse(int id, String username, String email, Set<Role> roles, String nameSalaryRate) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles.stream().map(Role::getName).collect(Collectors.toSet());
        this.nameSalaryRate = nameSalaryRate;
    }
}

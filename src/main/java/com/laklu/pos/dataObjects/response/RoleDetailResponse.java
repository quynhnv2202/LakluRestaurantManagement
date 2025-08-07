package com.laklu.pos.dataObjects.response;

import com.laklu.pos.entities.Profile;
import com.laklu.pos.entities.Role;
import com.laklu.pos.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDetailResponse {
    private Integer id;
    private String name;
    private String description;
    private int userCount;
    private List<PermissionResource> permissions;
    private List<UserDetailResponse> users;

    public RoleDetailResponse(Role role, Map<User, Profile> userProfileMap) {
        this.id = role.getId();
        this.name = role.getName();
        this.description = role.getDescription();
        Set<User> userSet = role.getUsers();
        this.userCount = userSet.size();
        this.permissions = role.getPermissions().stream().map(PermissionResource::new).toList();
        
        this.users = new ArrayList<>();
        for (User user : userSet) {
            Profile profile = userProfileMap.get(user);
            this.users.add(new UserDetailResponse(user, profile));
        }
    }
} 
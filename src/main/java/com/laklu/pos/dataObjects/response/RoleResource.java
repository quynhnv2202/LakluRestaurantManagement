package com.laklu.pos.dataObjects.response;

import com.laklu.pos.entities.Role;
import lombok.Data;

import java.util.List;

@Data
public class RoleResource
{
    private Integer id;
    private String name;
    private String description;
    private List<PermissionResource> permissions;

    public RoleResource(Role role) {
        this.id = role.getId();
        this.name = role.getName();
        this.description = role.getDescription();
        this.permissions = role.getPermissions().stream().map(PermissionResource::new).toList();
    }
}

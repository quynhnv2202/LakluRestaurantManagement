package com.laklu.pos.dataObjects.response;

import com.laklu.pos.entities.Permission;
import lombok.Data;

@Data
public class PermissionResource {
    private Integer id;
    private String name;

    public PermissionResource(Permission permission) {
        this.id = permission.getId();
        this.name = permission.getName();
    }
}

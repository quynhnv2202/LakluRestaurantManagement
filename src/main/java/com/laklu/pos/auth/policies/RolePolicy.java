package com.laklu.pos.auth.policies;

import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.entities.Role;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class RolePolicy implements Policy<Role>{
    @Override
    public boolean canCreate(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_ROLE);
    }

    @Override
    public boolean canEdit(UserPrincipal userPrincipal, Role role) {
        return userPrincipal.hasPermission(PermissionAlias.UPDATE_USER);
    }

    @Override
    public boolean canDelete(UserPrincipal userPrincipal, Role role) {
        return userPrincipal.hasPermission(PermissionAlias.DELETE_USER);
    }

    @Override
    public boolean canView(UserPrincipal userPrincipal, Role role) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_ROLE);
    }

    @Override
    public boolean canList(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_ROLE);
    }
}

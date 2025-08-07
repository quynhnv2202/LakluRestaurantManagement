package com.laklu.pos.auth.policies;

import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.entities.User;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.stereotype.Component;


@Component
public class UserPolicy implements Policy<User> {
    @Override
    public boolean canCreate(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_USER);
    }

    @Override
    public boolean canEdit(UserPrincipal userPrincipal, User user) {
        return userPrincipal.hasPermission(PermissionAlias.UPDATE_USER);
    }

    @Override
    public boolean canDelete(UserPrincipal userPrincipal, User user) {
        return userPrincipal.hasPermission(PermissionAlias.DELETE_USER);
    }

    @Override
    public boolean canView(UserPrincipal userPrincipal, User user) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_USER);
    }

    @Override
    public boolean canList(UserPrincipal userPrincipal) {
        return userPrincipal.hasAnyPermission(PermissionAlias.LIST_USER);
    }
}

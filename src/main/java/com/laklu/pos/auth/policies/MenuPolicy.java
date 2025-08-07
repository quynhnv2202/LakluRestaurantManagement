package com.laklu.pos.auth.policies;

import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.entities.Menu;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class MenuPolicy implements Policy<Menu> {

    @Override
    public boolean canCreate(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_MENU);
    }

    @Override
    public boolean canEdit(UserPrincipal userPrincipal, Menu menu) {
        return userPrincipal.hasPermission(PermissionAlias.UPDATE_MENU);
    }

    @Override
    public boolean canDelete(UserPrincipal userPrincipal, Menu menu) {
        return userPrincipal.hasPermission(PermissionAlias.DELETE_MENU);
    }

    @Override
    public boolean canView(UserPrincipal userPrincipal, Menu menu) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_MENU);
    }

    @Override
    public boolean canList(UserPrincipal userPrincipal) {
        return userPrincipal.hasAnyPermission(PermissionAlias.LIST_MENU);
    }

}

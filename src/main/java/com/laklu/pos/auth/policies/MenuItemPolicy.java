package com.laklu.pos.auth.policies;

import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.entities.MenuItem;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class MenuItemPolicy implements Policy<MenuItem> {

    @Override
    public boolean canCreate(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_MENU_ITEM);
    }

    @Override
    public boolean canEdit(UserPrincipal userPrincipal, MenuItem menuItem) {
        return userPrincipal.hasPermission(PermissionAlias.UPDATE_MENU_ITEM);
    }

    @Override
    public boolean canDelete(UserPrincipal userPrincipal, MenuItem menuItem) {
        return userPrincipal.hasPermission(PermissionAlias.DELETE_MENU_ITEM);
    }

    @Override
    public boolean canView(UserPrincipal userPrincipal, MenuItem menuItem) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_MENU_ITEM);
    }

    @Override
    public boolean canList(UserPrincipal userPrincipal) {
        return userPrincipal.hasAnyPermission(PermissionAlias.LIST_MENU_ITEM);
    }
}

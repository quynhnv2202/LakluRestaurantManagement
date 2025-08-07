package com.laklu.pos.auth.policies;

import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.entities.Table;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class TablePolicy implements Policy<Table> {
    @Override
    public boolean canCreate(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_TABLE);
    }

    @Override
    public boolean canEdit(UserPrincipal userPrincipal, Table table) {
        return userPrincipal.hasPermission(PermissionAlias.UPDATE_TABLE);
    }

    @Override
    public boolean canDelete(UserPrincipal userPrincipal, Table table) {
        return userPrincipal.hasPermission(PermissionAlias.DELETE_TABLE);
    }

    @Override
    public boolean canView(UserPrincipal userPrincipal, Table table) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_TABLE);
    }

    @Override
    public boolean canList(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_TABLE);
    }
}

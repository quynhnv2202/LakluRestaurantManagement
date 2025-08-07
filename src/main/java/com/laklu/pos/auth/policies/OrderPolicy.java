package com.laklu.pos.auth.policies;

import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.entities.Order;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class OrderPolicy implements Policy<Order> {
    @Override
    public boolean canCreate(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_ORDER);
    }

    @Override
    public boolean canEdit(UserPrincipal userPrincipal, Order order) {
        return userPrincipal.hasPermission(PermissionAlias.UPDATE_ORDER);
    }

    @Override
    public boolean canDelete(UserPrincipal userPrincipal, Order order) {
        return userPrincipal.hasPermission(PermissionAlias.DELETE_ORDER);
    }

    @Override
    public boolean canView(UserPrincipal userPrincipal, Order order) {
        return userPrincipal.hasPermission(PermissionAlias.VIEW_ORDER);
    }

    @Override
    public boolean canList(UserPrincipal userPrincipal) {
        return userPrincipal.hasAnyPermission(PermissionAlias.LIST_ORDER);
    }
}

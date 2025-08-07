package com.laklu.pos.auth.policies;

import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.entities.OrderItem;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class OrderItemPolicy  implements Policy<OrderItem> {
    @Override
    public boolean canCreate(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_ORDER_ITEM);
    }

    @Override
    public boolean canEdit(UserPrincipal userPrincipal,OrderItem orderItem) {
        return userPrincipal.hasPermission(PermissionAlias.UPDATE_ORDER_ITEM);
    }

    @Override
    public boolean canDelete(UserPrincipal userPrincipal, OrderItem orderItem) {
        return userPrincipal.hasPermission(PermissionAlias.DELETE_ORDER_ITEM);
    }

    @Override
    public boolean canView(UserPrincipal userPrincipal, OrderItem orderItem) {
        return userPrincipal.hasPermission(PermissionAlias.VIEW_ORDER_ITEM);
    }

    @Override
    public boolean canList(UserPrincipal userPrincipal) {
        return userPrincipal.hasAnyPermission(PermissionAlias.LIST_ORDER_ITEM);
    }
}

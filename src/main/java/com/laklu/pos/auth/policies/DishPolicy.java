package com.laklu.pos.auth.policies;

import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.entities.Dish;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class DishPolicy implements Policy<Dish> {
    @Override
    public boolean canCreate(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_DISH);
    }

    @Override
    public boolean canEdit(UserPrincipal userPrincipal, Dish dish) {
        return userPrincipal.hasPermission(PermissionAlias.UPDATE_DISH);
    }

    @Override
    public boolean canDelete(UserPrincipal userPrincipal, Dish dish) {
        return userPrincipal.hasPermission(PermissionAlias.DELETE_DISH);
    }

    @Override
    public boolean canView(UserPrincipal userPrincipal, Dish dish) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_DISH);
    }

    @Override
    public boolean canList(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_DISH);
    }
}

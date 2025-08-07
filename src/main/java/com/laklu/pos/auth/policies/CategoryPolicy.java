package com.laklu.pos.auth.policies;

import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.entities.Category;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class CategoryPolicy implements Policy<Category> {

    @Override
    public boolean canCreate(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_CATEGORY);
    }

    @Override
    public boolean canEdit(UserPrincipal userPrincipal, Category category) {
        return userPrincipal.hasPermission(PermissionAlias.UPDATE_CATEGORY);
    }

    @Override
    public boolean canDelete(UserPrincipal userPrincipal, Category category) {
        return userPrincipal.hasPermission(PermissionAlias.DELETE_CATEGORY);
    }

    @Override
    public boolean canView(UserPrincipal userPrincipal, Category category) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_CATEGORY);
    }

    @Override
    public boolean canList(UserPrincipal userPrincipal) {
        return userPrincipal.hasAnyPermission(PermissionAlias.LIST_CATEGORY);
    }
}

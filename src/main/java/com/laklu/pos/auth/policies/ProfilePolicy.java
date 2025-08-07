package com.laklu.pos.auth.policies;

import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.entities.Profile;
import com.laklu.pos.entities.User;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class ProfilePolicy implements Policy<Profile>{
    @Override
    public boolean canCreate(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_PROFILE);
    }

    @Override
    public boolean canEdit(UserPrincipal userPrincipal, Profile profile) {
        // Cho phép admin và chủ sở hữu profile có thể chỉnh sửa
        return userPrincipal.hasPermission(PermissionAlias.UPDATE_PROFILE) ||
               isOwner(userPrincipal, profile);
    }

    @Override
    public boolean canDelete(UserPrincipal userPrincipal, Profile profile) {
        return userPrincipal.hasPermission(PermissionAlias.DELETE_PROFILE);
    }

    @Override
    public boolean canView(UserPrincipal userPrincipal, Profile profile) {
        // Cho phép admin và chủ sở hữu profile có thể xem
        return userPrincipal.hasPermission(PermissionAlias.VIEW_PROFILE) ||
               isOwner(userPrincipal, profile);
    }

    @Override
    public boolean canList(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_PROFILE);
    }
    
    private boolean isOwner(UserPrincipal userPrincipal, Profile profile) {
        User user = userPrincipal.getPersitentUser();
        return profile.getUser().getId().equals(user.getId());
    }
}

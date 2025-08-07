package com.laklu.pos.auth.policies;

import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.entities.Attachment;
import com.laklu.pos.entities.Role;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class AttachmentPolicy implements Policy<Attachment>{
    @Override
    public boolean canCreate(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_ATTACHMENT);
    }

    @Override
    public boolean canEdit(UserPrincipal userPrincipal, Attachment attachment) {
        return userPrincipal.hasPermission(PermissionAlias.UPDATE_ATTACHMENT);
    }

    @Override
    public boolean canDelete(UserPrincipal userPrincipal, Attachment attachment) {
        return userPrincipal.hasPermission(PermissionAlias.DELETE_ATTACHMENT);
    }

    @Override
    public boolean canView(UserPrincipal userPrincipal, Attachment attachment) {
        return userPrincipal.hasPermission(PermissionAlias.VIEW_ATTACHMENT);
    }

    @Override
    public boolean canList(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_ATTACHMENT);
    }
}

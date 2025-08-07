package com.laklu.pos.auth.policies;

import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.entities.Voucher;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class VoucherPolicy implements Policy<Voucher>{

    @Override
    public boolean canCreate(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_VOUCHER);
    }

    @Override
    public boolean canEdit(UserPrincipal userPrincipal, Voucher voucher) {
        return userPrincipal.hasPermission(PermissionAlias.UPDATE_VOUCHER);
    }

    @Override
    public boolean canDelete(UserPrincipal userPrincipal, Voucher voucher) {
        return userPrincipal.hasPermission(PermissionAlias.DELETE_VOUCHER);
    }

    @Override
    public boolean canView(UserPrincipal userPrincipal, Voucher voucher) {
        return userPrincipal.hasPermission(PermissionAlias.VIEW_VOUCHER);
    }

    @Override
    public boolean canList(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_VOUCHER);
    }
}

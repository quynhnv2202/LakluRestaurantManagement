package com.laklu.pos.auth.policies;

import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.entities.Payment;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class PaymentPolicy implements Policy<Payment>{

    @Override
    public boolean canCreate(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_PAYMENT);
    }

    @Override
    public boolean canEdit(UserPrincipal userPrincipal, Payment payment) {
        return userPrincipal.hasPermission(PermissionAlias.CANCEL_PAYMENT);
    }

    @Override
    public boolean canList(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_PAYMENT);
    }

    @Override
    public boolean canDelete(UserPrincipal userPrincipal, Payment payment) {
        return userPrincipal.hasPermission(PermissionAlias.DELETE_PAYMENT);
    }

    @Override
    public boolean canView(UserPrincipal userPrincipal, Payment payment) {
        return userPrincipal.hasPermission(PermissionAlias.VIEW_PAYMENT);
    }
}

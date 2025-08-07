package com.laklu.pos.auth.policies;

import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.entities.Payslip;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class PayslipPolicy implements Policy<Payslip> {

    @Override
    public boolean canCreate(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_PAYSLIP);
    }

    @Override
    public boolean canEdit(UserPrincipal userPrincipal, Payslip payslip) {
        return false;
    }

    @Override
    public boolean canDelete(UserPrincipal userPrincipal, Payslip payslip) {
        return false;
    }

    @Override
    public boolean canView(UserPrincipal userPrincipal, Payslip payslip) {
        return userPrincipal.hasPermission(PermissionAlias.VIEW_PAYSLIP);
    }

    @Override
    public boolean canList(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_PAYSLIP);
    }
}

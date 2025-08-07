package com.laklu.pos.auth.policies;

import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.entities.Role;
import com.laklu.pos.entities.SalaryRate;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class SalaryRatePolicy implements Policy<SalaryRate>{
    @Override
    public boolean canCreate(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_SALARY_RATE);
    }

    @Override
    public boolean canEdit(UserPrincipal userPrincipal, SalaryRate salaryRate) {
        return userPrincipal.hasPermission(PermissionAlias.UPDATE_SALARY_RATE);
    }

    @Override
    public boolean canDelete(UserPrincipal userPrincipal, SalaryRate salaryRate) {
        return userPrincipal.hasPermission(PermissionAlias.DELETE_SALARY_RATE);
    }

    @Override
    public boolean canView(UserPrincipal userPrincipal, SalaryRate salaryRate) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_SALARY_RATE);
    }

    @Override
    public boolean canList(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_SALARY_RATE);
    }
}

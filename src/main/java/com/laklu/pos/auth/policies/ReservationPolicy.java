package com.laklu.pos.auth.policies;

import com.laklu.pos.entities.Reservation;
import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class ReservationPolicy implements Policy<Reservation> {

    @Override
    public boolean canCreate(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_RESERVATION);
    }

    @Override
    public boolean canEdit(UserPrincipal userPrincipal, Reservation reservation) {
        return userPrincipal.hasPermission(PermissionAlias.UPDATE_RESERVATION);
    }

    @Override
    public boolean canDelete(UserPrincipal userPrincipal, Reservation reservation) {
        return userPrincipal.hasPermission(PermissionAlias.DELETE_RESERVATION);
    }

    @Override
    public boolean canView(UserPrincipal userPrincipal, Reservation reservation) {
        return userPrincipal.hasPermission(PermissionAlias.VIEW_RESERVATION);
    }

    @Override
    public boolean canList(UserPrincipal userPrincipal) {
        return userPrincipal.hasAnyPermission(PermissionAlias.LIST_RESERVATION);
    }
}


package com.laklu.pos.auth.policies;

import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.entities.Schedule;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class SchedulePolicy implements Policy<Schedule> {
    @Override
    public boolean canCreate(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_SCHEDULE);
    }

    @Override
    public boolean canEdit(UserPrincipal userPrincipal, Schedule schedule) {
        return userPrincipal.hasPermission(PermissionAlias.UPDATE_SCHEDULE);
    }

    @Override
    public boolean canDelete(UserPrincipal userPrincipal, Schedule schedule) {
        return userPrincipal.hasPermission(PermissionAlias.DELETE_SCHEDULE);
    }

    @Override
    public boolean canView(UserPrincipal userPrincipal, Schedule schedule) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_SCHEDULE) ||schedule.getStaffs().contains(userPrincipal.getPersitentUser());
    }

    @Override
    public boolean canList(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.LIST_SCHEDULE);

    }

    public boolean canCreateCheckInQrCode(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_SCHEDULE_CHECK_IN_QR_CODE);
    }

    public boolean canCreateCheckOutQrCode(UserPrincipal userPrincipal) {
        return userPrincipal.hasPermission(PermissionAlias.CREATE_SCHEDULE_CHECK_OUT_QR_CODE);
    }

    public boolean canCheckIn(UserPrincipal userPrincipal, Schedule schedule) {
        return schedule.getStaffs().contains(userPrincipal.getPersitentUser());
    }

    public boolean canCheckOut(UserPrincipal userPrincipal, Schedule schedule) {
        return schedule.getStaffs().contains(userPrincipal.getPersitentUser());
    }
}

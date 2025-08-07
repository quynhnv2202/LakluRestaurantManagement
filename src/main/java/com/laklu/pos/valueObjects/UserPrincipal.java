package com.laklu.pos.valueObjects;


import com.laklu.pos.auth.PermissionAlias;
import com.laklu.pos.entities.Role;
import com.laklu.pos.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> roles = user.getRoles();
        return roles.stream()
                .map(Role::getPermissions)
                .flatMap(Set::stream)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }


    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getUsername();
    }

    public User getPersitentUser() {
        return this.user;
    }

    public List<String> pluckPermissionAlias() {
        return this.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toList());
    }

    public boolean hasAnyPermission(String... permissions) {
        return Arrays.stream(permissions)
                .anyMatch(permission -> this.pluckPermissionAlias().contains(permission));
    }

    public boolean hasAllPermissions(String... permissions) {
        return Arrays.stream(permissions)
                .allMatch(permission -> this.pluckPermissionAlias().contains(permission));
    }

    public boolean hasPermission(String permission) {
        return this.pluckPermissionAlias().contains(permission);
    }

    public boolean hasAnyPermission(PermissionAlias... permissions) {
        return Arrays.stream(permissions)
                .anyMatch(permission -> this.pluckPermissionAlias().contains(permission.getAlias()));
    }

    public boolean hasAllPermissions(PermissionAlias... permissions) {
        return Arrays.stream(permissions)
                .allMatch(permission -> this.pluckPermissionAlias().contains(permission.getAlias()));
    }

    public boolean hasPermission(PermissionAlias permission) {
        return this.pluckPermissionAlias().contains(permission.getAlias());
    }

}

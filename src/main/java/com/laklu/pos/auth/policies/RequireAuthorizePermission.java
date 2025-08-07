package com.laklu.pos.auth.policies;

import org.springframework.security.core.GrantedAuthority;

public class RequireAuthorizePermission implements GrantedAuthority {

    private final String permission;

    public RequireAuthorizePermission(String permission) {
        this.permission = permission;
    }

    @Override
    public String getAuthority() {
        return this.permission;
    }
}

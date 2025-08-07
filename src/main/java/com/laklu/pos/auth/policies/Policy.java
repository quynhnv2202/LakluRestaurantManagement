package com.laklu.pos.auth.policies;

import com.laklu.pos.valueObjects.UserPrincipal;

public interface Policy<T> {
    boolean canCreate(UserPrincipal userPrincipal);

    boolean canEdit(UserPrincipal userPrincipal, T t);

    boolean canDelete(UserPrincipal userPrincipal, T t);

    boolean canView(UserPrincipal userPrincipal, T t);

    boolean canList(UserPrincipal userPrincipal);
}

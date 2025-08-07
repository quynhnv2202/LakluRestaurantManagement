package com.laklu.pos.entities;

import com.laklu.pos.controllers.ActivityLogListener;
import com.laklu.pos.enums.PermissionGroup;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

@Setter
@Getter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
//@EntityListeners(ActivityLogListener.class)
public class Permission implements GrantedAuthority, Identifiable<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String alias;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private PermissionGroup permissionGroup;

    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getAuthority() {
        return this.getAlias();
    }
}

package com.laklu.pos.services;

import com.laklu.pos.dataObjects.request.NewRole;
import com.laklu.pos.dataObjects.request.UpdateRole;
import com.laklu.pos.dataObjects.response.RoleResponse;
import com.laklu.pos.entities.Permission;
import com.laklu.pos.entities.Role;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.repositories.PermissionRepository;
import com.laklu.pos.repositories.RoleRepository;
import com.laklu.pos.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public Role storeRole(NewRole role) {
        Role roleEntity = new Role();
        roleEntity.setName(role.getName());
        roleEntity.setDescription(role.getDescription());

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(role.getPermissions()));
        roleEntity.setPermissions(permissions);

        return roleRepository.save(roleEntity);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Optional<Role> findById(Integer id) {
        return roleRepository.findById(id);
    }

    public Role findOrFail(Integer id) {
        return this.findById(id).orElseThrow(NotFoundException::new);
    }

    public Role updateRole(UpdateRole updateRole, Role role) {
        role.setName(role.getName());
        role.setDescription(role.getDescription());

        // sync role
        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(updateRole.getPermissions()));
        role.setPermissions(permissions);

        return roleRepository.save(role);
    }

    public void deleteRole(Role role) {
        roleRepository.delete(role);
    }

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    public boolean isRoleInUse(Role role) {
        // Kiểm tra xem có bất kỳ người dùng nào đang sử dụng role này không
        return userRepository.existsByRolesContaining(role);
    }
}
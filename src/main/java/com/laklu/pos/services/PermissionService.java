package com.laklu.pos.services;

import com.laklu.pos.dataObjects.response.PermissionGroupResponse;
import com.laklu.pos.dataObjects.response.PermissionResponse;
import com.laklu.pos.entities.Permission;
import com.laklu.pos.enums.PermissionGroup;
import com.laklu.pos.repositories.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public List<Permission> getAll() {
        List<Permission> permissions = permissionRepository.findAll();

        return permissions;
    }

    public void updateDescription(int id, String description) {
        Permission permission = permissionRepository.findById(id).orElseThrow(() -> new RuntimeException("Permission not found"));
        permission.setDescription(description);
        permissionRepository.save(permission);
    }
}

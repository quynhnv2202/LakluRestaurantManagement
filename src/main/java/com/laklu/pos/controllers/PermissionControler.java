package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.UserPolicy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.response.PermissionGroupResponse;
import com.laklu.pos.dataObjects.response.PermissionResponse;
import com.laklu.pos.entities.Permission;
import com.laklu.pos.enums.PermissionGroup;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.services.PermissionService;
import com.laklu.pos.uiltis.Ultis;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "Permission Controller", description = "Quản lý quyền")
@AllArgsConstructor
public class PermissionControler {

    private final PermissionService permissionService;
    private final UserPolicy userPolicy;

    @Operation(summary = "Lấy danh sách quyền", description = "API này dùng để lấy toàn bộ các quyền")
    @GetMapping("/")
    public ApiResponseEntity index() throws Exception {
        Ultis.throwUnless(userPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());

        List<Permission> permissions = permissionService.getAll();
        Map<PermissionGroup, List<Permission>> permissionMap = permissions.stream()
                .collect(groupingBy(Permission::getPermissionGroup));

        List<PermissionGroupResponse> result = new ArrayList<>();
        for (Map.Entry<PermissionGroup, List<Permission>> entry : permissionMap.entrySet()) {
            PermissionGroup group = entry.getKey();
            List<PermissionResponse> permissionDTOs = entry.getValue().stream()
                    .map(p -> new PermissionResponse(p.getId(), p.getAlias(), p.getName(), p.getDescription()))
                    .collect(Collectors.toList());

            result.add(new PermissionGroupResponse(
                    group.getLabel(),
                    group.getAlias(),
                    group.getDescription(),
                    permissionDTOs
            ));
        }
        return ApiResponseEntity.success(result);
    }

    @Operation(summary = "Cập nhật mô tả quyền", description = "API này dùng để cập nhật mô tả quyền")
    @PutMapping("/{id}")
    public ApiResponseEntity updateDescription(@PathVariable int id,@RequestBody String description) throws Exception {
        Ultis.throwUnless(userPolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());

        permissionService.updateDescription(id, description);

        return ApiResponseEntity.success("Permission description updated successfully");
    }
}

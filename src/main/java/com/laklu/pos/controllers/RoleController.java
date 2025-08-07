package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.RolePolicy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.NewRole;
import com.laklu.pos.dataObjects.request.UpdateRole;
import com.laklu.pos.dataObjects.response.RoleDetailResponse;
import com.laklu.pos.dataObjects.response.RoleResource;
import com.laklu.pos.dataObjects.response.RoleResponse;
import com.laklu.pos.entities.Profile;
import com.laklu.pos.entities.Role;
import com.laklu.pos.entities.User;
import com.laklu.pos.services.ProfileService;
import com.laklu.pos.services.RoleService;
import com.laklu.pos.validator.ValueExistIn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.laklu.pos.uiltis.Ultis;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Role Controller", description = "Quản lý vai trò của nhân viên")
@AllArgsConstructor
public class RoleController {

    private final RolePolicy rolePolicy;
    private final RoleService roleService;
    private final ProfileService profileService;

    @Operation(summary = "Tạo vai trò mới")
    @PostMapping("/")
    public ApiResponseEntity store(@RequestBody @Validated NewRole role) {
        rolePolicy.canCreate(JwtGuard.userPrincipal());

        this.validateName(role.getName());

        Role persitedRole = roleService.storeRole(role);

        return ApiResponseEntity.success(new RoleResource(persitedRole));
    }

    @Operation(summary = "Hiện thị toàn bộ role")
    @GetMapping("/")
    public ApiResponseEntity index() {
        rolePolicy.canList(JwtGuard.userPrincipal());

        List<Role> roles = roleService.getAllRoles();

        List<RoleResponse> roleResponses = roles.stream()
                .map(role -> new RoleResponse(
                        role.getId(),
                        role.getName(),
                        role.getDescription(),
                        role.getUsers().size()
                ))
                .collect(Collectors.toList());

        return ApiResponseEntity.success(roleResponses);
    }

    @Operation(summary = "Hiển thị role theo id và thông tin chi tiết người dùng")
    @GetMapping("/{id}")
    public ApiResponseEntity show(@PathVariable int id) {
        Role role = roleService.findOrFail(id);

        rolePolicy.canView(JwtGuard.userPrincipal(), role);

        Set<User> users = role.getUsers();
        Map<User, Profile> userProfileMap = profileService.findProfilesByUsers(users);

        return ApiResponseEntity.success(new RoleDetailResponse(role, userProfileMap));
    }

    @Operation(summary = "Update role")
    @PutMapping("/{id}")
    public ApiResponseEntity update(@PathVariable int id, @RequestBody @Validated UpdateRole updateRole) {
        Role roleToUpdate = roleService.findOrFail(id);

        rolePolicy.canEdit(JwtGuard.userPrincipal(), roleToUpdate);

        //this.validateName(updateRole.getName());

        Role updatedRole = roleService.updateRole(updateRole, roleToUpdate);

        return ApiResponseEntity.success(new RoleResource(updatedRole));
    }

    @Operation(summary = "Xoá role theo id")
    @DeleteMapping("/{id}")
    public ApiResponseEntity delete(@PathVariable int id) throws Exception {
        Role role = roleService.findOrFail(id);
        
        Ultis.throwUnless(rolePolicy.canDelete(JwtGuard.userPrincipal(), role), new ForbiddenException());
        
        // Kiểm tra xem có người dùng nào đang sử dụng role này không
        if (roleService.isRoleInUse(role)) {
            return ApiResponseEntity.exception(HttpStatus.BAD_REQUEST, "Lỗi xóa vai trò", "Không thể xóa vai trò đang được sử dụng bởi một hoặc nhiều người dùng");
        }

        roleService.deleteRole(role);

        return ApiResponseEntity.success("Xóa vai trò thành công");
    }

    private void validateName(String name) {
        ValueExistIn<String> rule = new ValueExistIn<>(
                "Vai trò",
                name,
                (n) -> roleService.findByName(n).isEmpty()
        );
        if (!rule.isValid()) {
            throw new IllegalArgumentException(rule.getMessage());
        }
    }
}

package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.Policy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.NewUser;
import com.laklu.pos.dataObjects.request.UpdateUser;
import com.laklu.pos.dataObjects.request.ChangePasswordRequest;
import com.laklu.pos.dataObjects.request.AdminChangePasswordRequest;
import com.laklu.pos.dataObjects.request.UpdateUserWithProfileRequest;
import com.laklu.pos.dataObjects.request.UpdateProfileRequest;
import com.laklu.pos.dataObjects.response.AuthUserResponse;
import com.laklu.pos.dataObjects.response.ProfileResponse;
import com.laklu.pos.dataObjects.response.UserResponse;
import com.laklu.pos.dataObjects.response.UserWithProfileResponse;
import com.laklu.pos.entities.User;
import com.laklu.pos.entities.SalaryRate;
import com.laklu.pos.entities.Profile;
import com.laklu.pos.entities.Role;
import com.laklu.pos.enums.EmploymentStatus;
import com.laklu.pos.exceptions.httpExceptions.BadRequestException;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.mapper.UserMapper;
import com.laklu.pos.mapper.ProfileMapper;
import com.laklu.pos.repositories.RoleRepository;
import com.laklu.pos.services.SalaryRateService;
import com.laklu.pos.services.UserService;
import com.laklu.pos.services.ProfileService;
import com.laklu.pos.uiltis.Ultis;
import com.laklu.pos.validator.RuleValidator;
import com.laklu.pos.validator.UsernameMustBeUnique;
import com.laklu.pos.valueObjects.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.HashSet;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Controller", description = "Quản lý thông tin người dùng")
@AllArgsConstructor
public class UserController {

    private final Policy<User> userPolicy;
    private final UserService userService;
    private final UserMapper userMapper;
    private final SalaryRateService salaryRateService;
    private final ProfileService profileService;
    private final ProfileMapper profileMapper;
    private final RoleRepository roleRepository;

    @Operation(summary = "Lấy thông tin tất cả người dùng", description = "API này dùng để lấy thông tin tất cả nhân viên")
    @GetMapping("/")
    public ApiResponseEntity index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) throws Exception {
        Ultis.throwUnless(userPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<User> userPage = userService.getAllPaginated(pageable);
        
        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRoles(),
                        user.getSalaryRate() != null ? user.getSalaryRate().getLevelName() : null
                ))
                .toList();
                
        Map<String, Object> response = new HashMap<>();
        response.put("users", userResponses);
        response.put("currentPage", userPage.getNumber());
        response.put("totalItems", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());

        return ApiResponseEntity.success(response);
    }

    @Operation(summary = "Tạo một người dùng mới", description = "API này dùng để tạo một người dùng mới")
    @PostMapping("/")
    public ApiResponseEntity store(@RequestBody @Validated NewUser user) throws Exception {
        System.out.println("Received user: " + user);
        Ultis.throwUnless(userPolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());

        Function<String, Optional<User>> userResolver = userService::findByUsername;

        RuleValidator.validate(new UsernameMustBeUnique(userResolver, user.getUsername()));

        User persistedUser = userService.store(user);
        
        // Create an empty profile for the new user
        Profile profile = new Profile();
        profile.setUser(persistedUser);
        profile.setEmploymentStatus(EmploymentStatus.WORKING);
        profile.setDepartment(user.getDepartment());
        profile.setHireDate(LocalDateTime.now());
        profileService.createProfile(profile, persistedUser.getId());

        return ApiResponseEntity.success(new AuthUserResponse(new UserPrincipal(persistedUser)));
    }

    @Operation(summary = "Lấy thông tin người dùng theo id", description = "API này dùng để lấy thông tin nhân viên theo id")
    @GetMapping("/{id}")
    public ApiResponseEntity show(@PathVariable int id) throws Exception {
        var user = userService.findOrFail(id);

        Ultis.throwUnless(userPolicy.canView(JwtGuard.userPrincipal(), user), new ForbiddenException());

        return ApiResponseEntity.success(new AuthUserResponse(new UserPrincipal(user)));
    }

    @Operation(summary = "Lấy thông tin chi tiết người dùng kèm profile theo id", description = "API này dùng để lấy thông tin chi tiết người dùng bao gồm cả profile theo id")
    @GetMapping("/{id}/with-profile")
    public ApiResponseEntity getUserWithProfile(@PathVariable int id) throws Exception {
        var user = userService.findOrFail(id);

        Ultis.throwUnless(userPolicy.canView(JwtGuard.userPrincipal(), user), new ForbiddenException());
        
        // Lấy profile của người dùng
        Profile profile = profileService.findByUserId(id).orElse(null);
        
        ProfileResponse profileResponse = null;
        if (profile != null) {
            profileResponse = profileMapper.toProfileResponse(profile);
        }
        
        UserWithProfileResponse response = UserWithProfileResponse.fromUserAndProfile(user, profileResponse);
        
        return ApiResponseEntity.success(response);
    }

    @Operation(summary = "Cập nhật thông tin tất cả người dùng", description = "API này dùng để cập nhật thông tin người dùng")
    @PutMapping("/{id}")
    public ApiResponseEntity update(@PathVariable int id, @RequestBody @Validated UpdateUser updateUser) throws Exception {
        var existingUser = userService.findOrFail(id);

        Ultis.throwUnless(userPolicy.canEdit(JwtGuard.userPrincipal(), existingUser), new ForbiddenException());

        userMapper.updateUserFromDto(updateUser, existingUser);

        if (updateUser.getSalaryRateId() != null) {
            SalaryRate salaryRate = salaryRateService.findOrFail(updateUser.getSalaryRateId());
            existingUser.setSalaryRate(salaryRate);
        }

        User updatedUser = userService.update(existingUser);
        return ApiResponseEntity.success(new AuthUserResponse(new UserPrincipal(updatedUser)));
    }

    @Operation(summary = "Xoá người dùng theo id", description = "API này dùng để xoá người dùng")
    @DeleteMapping("/{id}")
    public ApiResponseEntity delete(@PathVariable int id) throws Exception {
        var user = userService.findOrFail(id);

        Ultis.throwUnless(userPolicy.canDelete(JwtGuard.userPrincipal(), user), new ForbiddenException());

        userService.deleteUser(user);

        return ApiResponseEntity.success("Xóa người dùng thành công");
    }
    
    @Operation(summary = "Đổi mật khẩu (dành cho người dùng)", description = "API này cho phép người dùng đổi mật khẩu của chính họ, yêu cầu mật khẩu cũ để xác thực")
    @PostMapping("/change-password")
    public ApiResponseEntity changePassword(@RequestBody @Validated ChangePasswordRequest request) throws Exception {
        UserPrincipal currentUser = JwtGuard.userPrincipal();
        
        if (currentUser == null) {
            throw new ForbiddenException();
        }
        
        User user = currentUser.getPersitentUser();
        boolean success = userService.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());
        
        if (!success) {
            return ApiResponseEntity.exception(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không chính xác");
        }
        
        return ApiResponseEntity.success("Đổi mật khẩu thành công");
    }
    
    @Operation(summary = "Đổi mật khẩu (dành cho quản lý)", description = "API này cho phép quản lý đổi mật khẩu của người dùng khác mà không cần biết mật khẩu cũ")
    @PostMapping("/{id}/admin-change-password")
    public ApiResponseEntity adminChangePassword(@PathVariable int id, @RequestBody @Validated AdminChangePasswordRequest request) throws Exception {
        var user = userService.findOrFail(id);
        
        Ultis.throwUnless(userPolicy.canEdit(JwtGuard.userPrincipal(), user), new ForbiddenException());
        
        userService.adminChangePassword(id, request.getNewPassword());
        
        return ApiResponseEntity.success("Đổi mật khẩu thành công");
    }

    @Operation(summary = "Lấy thông tin tất cả người dùng cùng với profile", description = "API này dùng để lấy thông tin chi tiết tất cả nhân viên bao gồm profile")
    @GetMapping("/with-profile")
    public ApiResponseEntity getAllUsersWithProfile(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) throws Exception {
        Ultis.throwUnless(userPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<User> userPage = userService.getAllPaginated(pageable);
        Set<User> userSet = new HashSet<>(userPage.getContent());
        
        // Lấy tất cả profile theo user
        Map<User, Profile> userProfileMap = profileService.findProfilesByUsers(userSet);
        
        // Chuyển đổi sang DTO response
        List<UserWithProfileResponse> responseList = userPage.getContent().stream()
                .map(user -> {
                    Profile profile = userProfileMap.get(user);
                    ProfileResponse profileResponse = null;
                    
                    if (profile != null) {
                        profileResponse = profileMapper.toProfileResponse(profile);
                    }
                    
                    return UserWithProfileResponse.fromUserAndProfile(user, profileResponse);
                })
                .toList();
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", responseList);
        response.put("currentPage", userPage.getNumber());
        response.put("totalItems", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());
        
        return ApiResponseEntity.success(response);
    }
    
    @Operation(summary = "Cập nhật thông tin đầy đủ người dùng bao gồm cả profile", description = "API này dùng để cập nhật toàn bộ thông tin người dùng và profile cùng một lúc")
    @PutMapping("/{id}/with-profile")
    public ApiResponseEntity updateUserWithProfile(@PathVariable int id, @RequestBody @Validated UpdateUserWithProfileRequest request) throws Exception {
        // Lấy thông tin người dùng hiện tại
        User existingUser = userService.findOrFail(id);
        
        // Kiểm tra quyền
        Ultis.throwUnless(userPolicy.canEdit(JwtGuard.userPrincipal(), existingUser), new ForbiddenException());
        
        // Cập nhật thông tin người dùng
        UpdateUser updateUser = new UpdateUser();
        updateUser.setEmail(request.getEmail());
        updateUser.setPhone(request.getPhone());
        updateUser.setRoleIds(request.getRoleIds());
        updateUser.setSalaryRateId(request.getSalaryRateId());
        
        userMapper.updateUserFromDto(updateUser, existingUser);
        
        if (request.getSalaryRateId() != null) {
            SalaryRate salaryRate = salaryRateService.findOrFail(request.getSalaryRateId());
            existingUser.setSalaryRate(salaryRate);
        }
        
        // Cập nhật roles trước khi lưu user
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> newRoles = new HashSet<>();
            
            try {
                for (Integer roleId : request.getRoleIds()) {
                    Optional<Role> roleOpt = roleRepository.findRoleById(roleId);
                    if (roleOpt.isPresent()) {
                        newRoles.add(roleOpt.get());
                    } else {
                        // Thay vì ném ra NotFoundException, log lỗi và bỏ qua
                        System.out.println("Không tìm thấy role với ID: " + roleId);
                    }
                }
                
                if (!newRoles.isEmpty()) {
                    existingUser.setRoles(newRoles);
                } else {
                    throw new BadRequestException();
                }
            } catch (Exception e) {
                if (!(e instanceof BadRequestException)) {
                    throw new BadRequestException();
                } else {
                    throw e;
                }
            }
        }
        
        // Lưu thông tin người dùng
        User updatedUser = userService.update(existingUser);
        
        // Lấy và cập nhật thông tin profile
        Profile profile = profileService.findByUserId(id).orElse(null);
        
        // Tạo mới profile nếu chưa có
        if (profile == null) {
            profile = new Profile();
            profile.setUser(updatedUser);
            profile.setEmploymentStatus(request.getEmploymentStatus() != null ? 
                                        request.getEmploymentStatus() : EmploymentStatus.WORKING);
        }
            
        // Tạo đối tượng UpdateProfileRequest từ thông tin request
        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest();
        updateProfileRequest.setFullName(request.getFullName());
        updateProfileRequest.setGender(request.getGender());
        updateProfileRequest.setDateOfBirth(request.getDateOfBirth());
        updateProfileRequest.setPhoneNumber(request.getPhoneNumber());
        updateProfileRequest.setAddress(request.getAddress());
        updateProfileRequest.setDepartment(request.getDepartment());
        updateProfileRequest.setHireDate(request.getHireDate());
        updateProfileRequest.setBankAccount(request.getBankAccount());
        updateProfileRequest.setBankNumber(request.getBankNumber());
        
        // Cập nhật profile
        profileMapper.updateProfileFromDto(updateProfileRequest, profile);
        
        // Cập nhật trạng thái làm việc nếu có
        if (request.getEmploymentStatus() != null) {
            profile.setEmploymentStatus(request.getEmploymentStatus());
        }
        
        // Lưu profile tùy thuộc vào profile mới hoặc đã tồn tại
        if (profile.getId() == null) {
            profile = profileService.createProfile(profile, updatedUser.getId());
        } else {
            profile = profileService.updateProfile(profile);
        }
        
        // Trả về thông tin người dùng đã cập nhật kèm theo profile
        ProfileResponse profileResponse = profileMapper.toProfileResponse(profile);
        
        UserWithProfileResponse response = UserWithProfileResponse.fromUserAndProfile(updatedUser, profileResponse);
        
        return ApiResponseEntity.success(response);
    }
}

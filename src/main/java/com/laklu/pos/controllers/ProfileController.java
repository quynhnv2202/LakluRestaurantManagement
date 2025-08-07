package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.ProfilePolicy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.ChangeEmploymentStatusRequest;
import com.laklu.pos.dataObjects.request.CreateProfileRequest;
import com.laklu.pos.dataObjects.request.UpdateAvatarRequest;
import com.laklu.pos.dataObjects.request.UpdateProfileRequest;
import com.laklu.pos.dataObjects.response.PersistAttachmentResponse;
import com.laklu.pos.dataObjects.response.ProfileResponse;
import com.laklu.pos.entities.Profile;
import com.laklu.pos.entities.User;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.mapper.ProfileMapper;
import com.laklu.pos.services.AttachmentService;
import com.laklu.pos.services.ProfileService;
import com.laklu.pos.services.UserService;
import com.laklu.pos.uiltis.Ultis;
import com.laklu.pos.valueObjects.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/profiles")
@Tag(name = "Profile Controller", description = "Quản lý thông tin cá nhân của nhân viên")
@AllArgsConstructor
public class ProfileController {
    
    private final ProfileService profileService;
    private final UserService userService;
    private final ProfileMapper profileMapper;
    private final ProfilePolicy profilePolicy;
    private final AttachmentService attachmentService;
    
    @Operation(summary = "Lấy tất cả profiles", description = "API này dùng để lấy tất cả thông tin cá nhân của nhân viên")
    @GetMapping
    public ApiResponseEntity getAllProfiles() throws Exception {
        UserPrincipal userPrincipal = JwtGuard.userPrincipal();
        Ultis.throwUnless(profilePolicy.canList(userPrincipal), new ForbiddenException());
        
        List<Profile> profiles = profileService.findAllActiveProfiles();
        List<ProfileResponse> responses = profiles.stream()
                .map(profileMapper::toProfileResponse)
                .collect(Collectors.toList());
        
        return ApiResponseEntity.success(responses);
    }
    
    @Operation(summary = "Lấy tất cả profiles bao gồm cả đã nghỉ việc", description = "API này dùng để lấy tất cả thông tin cá nhân của nhân viên bao gồm cả đã nghỉ việc")
    @GetMapping("/all")
    public ApiResponseEntity getAllProfilesIncludingResigned() throws Exception {
        UserPrincipal userPrincipal = JwtGuard.userPrincipal();
        Ultis.throwUnless(profilePolicy.canList(userPrincipal), new ForbiddenException());
        
        List<Profile> profiles = profileService.getAllProfiles();
        List<ProfileResponse> responses = profiles.stream()
                .map(profileMapper::toProfileResponse)
                .collect(Collectors.toList());
        
        return ApiResponseEntity.success(responses);
    }
    
    @Operation(summary = "Lấy profile theo id", description = "API này dùng để lấy thông tin cá nhân theo id")
    @GetMapping("/{id}")
    public ApiResponseEntity getProfileById(@PathVariable Integer id) throws Exception {
        UserPrincipal userPrincipal = JwtGuard.userPrincipal();
        Profile profile = profileService.findOrFail(id);
        
        Ultis.throwUnless(profilePolicy.canView(userPrincipal, profile), new ForbiddenException());
        
        ProfileResponse response = profileMapper.toProfileResponse(profile);
        return ApiResponseEntity.success(response);
    }
    
    @Operation(summary = "Lấy profile của user hiện tại", description = "API này dùng để lấy thông tin cá nhân của user đang đăng nhập")
    @GetMapping("/me")
    public ApiResponseEntity getMyProfile() throws Exception {
        UserPrincipal userPrincipal = JwtGuard.userPrincipal();
        User currentUser = userPrincipal.getPersitentUser();
        Profile profile = profileService.findByUserId(currentUser.getId())
                .orElseThrow(NotFoundException::new);
        
        // User luôn có quyền xem profile của mình
        Ultis.throwUnless(profilePolicy.canView(userPrincipal, profile), new ForbiddenException());
        
        ProfileResponse response = profileMapper.toProfileResponse(profile);
        return ApiResponseEntity.success(response);
    }
    
    @Operation(summary = "Lấy profile theo user id", description = "API này dùng để lấy thông tin cá nhân theo user id")
    @GetMapping("/user/{userId}")
    public ApiResponseEntity getProfileByUserId(@PathVariable Integer userId) throws Exception {
        UserPrincipal userPrincipal = JwtGuard.userPrincipal();
        Profile profile = profileService.findByUserIdOrFail(userId);
        
        Ultis.throwUnless(profilePolicy.canView(userPrincipal, profile), new ForbiddenException());
        
        ProfileResponse response = profileMapper.toProfileResponse(profile);
        return ApiResponseEntity.success(response);
    }
    
    @Operation(summary = "Tạo profile mới", description = "API này dùng để tạo thông tin cá nhân mới cho nhân viên")
    @PostMapping
    public ApiResponseEntity createProfile(@Valid @RequestBody CreateProfileRequest request) throws Exception {
        UserPrincipal userPrincipal = JwtGuard.userPrincipal();
        Ultis.throwUnless(profilePolicy.canCreate(userPrincipal), new ForbiddenException());
        
        Profile profile = profileMapper.toProfile(request);
        Profile createdProfile = profileService.createProfile(profile, request.getUserId());
        
        ProfileResponse response = profileMapper.toProfileResponse(createdProfile);
        return ApiResponseEntity.success(response);
    }
    
    @Operation(summary = "Cập nhật profile", description = "API này dùng để cập nhật thông tin cá nhân")
    @PutMapping("/{id}")
    public ApiResponseEntity updateProfile(@PathVariable Integer id, @Valid @RequestBody UpdateProfileRequest request) throws Exception {
        UserPrincipal userPrincipal = JwtGuard.userPrincipal();
        Profile profile = profileService.findOrFail(id);
        
        Ultis.throwUnless(profilePolicy.canEdit(userPrincipal, profile), new ForbiddenException());
        
        profileMapper.updateProfileFromDto(request, profile);
        Profile updatedProfile = profileService.updateProfile(profile);
        
        ProfileResponse response = profileMapper.toProfileResponse(updatedProfile);
        return ApiResponseEntity.success(response);
    }
    
    @Operation(summary = "Thay đổi trạng thái làm việc của nhân viên", description = "API này dùng để cập nhật trạng thái làm việc của nhân viên")
    @PutMapping("/{id}/status")
    public ApiResponseEntity changeEmploymentStatus(@PathVariable Integer id, @Valid @RequestBody ChangeEmploymentStatusRequest request) throws Exception {
        UserPrincipal userPrincipal = JwtGuard.userPrincipal();
        Profile profile = profileService.findOrFail(id);
        
        Ultis.throwUnless(profilePolicy.canEdit(userPrincipal, profile), new ForbiddenException());
        
        Profile updatedProfile = profileService.changeEmploymentStatus(id, request.getEmploymentStatus());
        ProfileResponse response = profileMapper.toProfileResponse(updatedProfile);
        
        return ApiResponseEntity.success(response);
    }
    
    @Operation(summary = "Chuyển trạng thái nhân viên thành đã nghỉ việc", description = "API này dùng để đánh dấu nhân viên đã nghỉ việc")
    @DeleteMapping("/{id}")
    public ApiResponseEntity deleteProfile(@PathVariable Integer id) throws Exception {
        UserPrincipal userPrincipal = JwtGuard.userPrincipal();
        Profile profile = profileService.findOrFail(id);
        
        Ultis.throwUnless(profilePolicy.canDelete(userPrincipal, profile), new ForbiddenException());
        
        profileService.deleteProfile(id);
        return ApiResponseEntity.success("Đã chuyển trạng thái nhân viên thành đã nghỉ việc");
    }
    
    @Operation(summary = "Cập nhật avatar", description = "API này dùng để cập nhật ảnh đại diện của nhân viên")
    @PutMapping("/{id}/avatar")
    public ApiResponseEntity updateAvatar(@PathVariable Integer id, @Valid @RequestBody UpdateAvatarRequest request) throws Exception {
        UserPrincipal userPrincipal = JwtGuard.userPrincipal();
        Profile profile = profileService.findOrFail(id);
        
        Ultis.throwUnless(profilePolicy.canEdit(userPrincipal, profile), new ForbiddenException());
        
        Profile updatedProfile = profileService.updateAvatar(id, request.getAttachmentIds());
        ProfileResponse response = toProfileResponse(updatedProfile);
        
        return ApiResponseEntity.success(response);
    }
    
    @Operation(summary = "Cập nhật avatar của user hiện tại", description = "API này dùng để cập nhật ảnh đại diện của user đang đăng nhập")
    @PutMapping("/me/avatar")
    public ApiResponseEntity updateMyAvatar(@Valid @RequestBody UpdateAvatarRequest request) throws Exception {
        UserPrincipal userPrincipal = JwtGuard.userPrincipal();
        User currentUser = userPrincipal.getPersitentUser();
        Profile profile = profileService.findByUserId(currentUser.getId())
                .orElseThrow(NotFoundException::new);
        
        // User luôn có quyền cập nhật avatar của mình
        Ultis.throwUnless(profilePolicy.canEdit(userPrincipal, profile), new ForbiddenException());
        
        Profile updatedProfile = profileService.updateAvatar(profile.getId(), request.getAttachmentIds());
        ProfileResponse response = toProfileResponse(updatedProfile);
        
        return ApiResponseEntity.success(response);
    }
    
    @Operation(summary = "Upload và cập nhật avatar", description = "API này dùng để upload và cập nhật ảnh đại diện")
    @PostMapping(value = "/{id}/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseEntity uploadAndUpdateAvatar(@PathVariable Integer id, @RequestParam("file") MultipartFile file) throws Exception {
        UserPrincipal userPrincipal = JwtGuard.userPrincipal();
        Profile profile = profileService.findOrFail(id);
        
        Ultis.throwUnless(profilePolicy.canEdit(userPrincipal, profile), new ForbiddenException());
        
        // Thêm phương thức để xử lý upload và cập nhật avatar
        Profile updatedProfile = profileService.uploadAndUpdateAvatar(id, file);
        ProfileResponse response = toProfileResponse(updatedProfile);
        
        return ApiResponseEntity.success(response);
    }
    
    @Operation(summary = "Upload và cập nhật avatar của user hiện tại", description = "API này dùng để upload và cập nhật ảnh đại diện của user đang đăng nhập")
    @PostMapping(value = "/me/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseEntity uploadAndUpdateMyAvatar(@RequestParam("file") MultipartFile file) throws Exception {
        UserPrincipal userPrincipal = JwtGuard.userPrincipal();
        User currentUser = userPrincipal.getPersitentUser();
        Profile profile = profileService.findByUserId(currentUser.getId())
                .orElseThrow(NotFoundException::new);
        
        // User luôn có quyền cập nhật avatar của mình
        Ultis.throwUnless(profilePolicy.canEdit(userPrincipal, profile), new ForbiddenException());
        
        Profile updatedProfile = profileService.uploadAndUpdateAvatar(profile.getId(), file);
        ProfileResponse response = toProfileResponse(updatedProfile);
        
        return ApiResponseEntity.success(response);
    }

    private ProfileResponse toProfileResponse(Profile profile) {
        ProfileResponse response = profileMapper.toProfileResponse(profile);
        if (profile.getAttachments() != null && !profile.getAttachments().isEmpty()) {
            List<PersistAttachmentResponse> avatarImages = profile.getAttachments().stream()
                    .map(attachmentService::toPersistAttachmentResponse)
                    .collect(Collectors.toList());
            response.setAvatarImages(avatarImages);
        }
        return response;
    }
}

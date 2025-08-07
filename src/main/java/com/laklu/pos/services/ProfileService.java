package com.laklu.pos.services;

import com.laklu.pos.entities.Attachment;
import com.laklu.pos.entities.Profile;
import com.laklu.pos.entities.User;
import com.laklu.pos.enums.EmploymentStatus;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.repositories.ProfileRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashMap;

@Service
@AllArgsConstructor
public class ProfileService {
    
    private final ProfileRepository profileRepository;
    private final UserService userService;
    private final AttachmentService attachmentService;
    
    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }
    
    public List<Profile> findAllActiveProfiles() {
        return profileRepository.findByEmploymentStatusNot(EmploymentStatus.RESIGNED);
    }
    
    public Optional<Profile> findById(Integer id) {
        return profileRepository.findById(id);
    }
    
    public Optional<Profile> findByUserId(Integer userId) {
        return profileRepository.findByUserId(userId);
    }
    
    public Profile findOrFail(Integer id) {
        return findById(id).orElseThrow(NotFoundException::new);
    }
    
    public Profile findByUserIdOrFail(Integer userId) {
        return findByUserId(userId).orElseThrow(NotFoundException::new);
    }
    
    @Transactional
    public Profile createProfile(Profile profile, Integer userId) {
        User user = userService.findOrFail(userId);
        profile.setUser(user);
        return profileRepository.save(profile);
    }
    
    @Transactional
    public Profile updateProfile(Profile profile) {
        return profileRepository.save(profile);
    }
    
    @Transactional
    public void deleteProfile(Integer id) {
        Profile profile = findOrFail(id);
        profile.setEmploymentStatus(EmploymentStatus.RESIGNED);
        profileRepository.save(profile);
    }
    
    @Transactional
    public Profile changeEmploymentStatus(Integer id, EmploymentStatus status) {
        Profile profile = findOrFail(id);
        profile.setEmploymentStatus(status);
        return profileRepository.save(profile);
    }
    
    @Transactional
    public Profile updateAvatar(Integer profileId, List<Long> attachmentIds) {
        Profile profile = findOrFail(profileId);
        
        // Xóa các attachment cũ của profile
        if (profile.getAttachments() != null && !profile.getAttachments().isEmpty()) {
            // Lưu lại các ID cần xóa
            List<Long> oldAttachmentIds = profile.getAttachments().stream()
                    .map(Attachment::getId)
                    .toList();
            
            // Xóa liên kết trong bảng trung gian
            profile.setAttachments(null);
            profileRepository.save(profile);
            
            // Xóa các attachment khỏi database
            for (Long id : oldAttachmentIds) {
                attachmentService.deleteAttachment(id);
            }
        }
        
        // Liên kết attachment với profile
        attachmentService.saveAttachment(profile, attachmentIds, true);
        
        // Cập nhật trường avatar sử dụng attachmentId đầu tiên nếu có
        if (!attachmentIds.isEmpty()) {
            profile.setAvatar(attachmentService.getImageUrl(attachmentIds.get(0)));
        }
        
        return profileRepository.save(profile);
    }
    
    @Transactional
    public Profile uploadAndUpdateAvatar(Integer profileId, MultipartFile file) throws IOException {
        Profile profile = findOrFail(profileId);
        
        // Xóa các attachment cũ của profile
        if (profile.getAttachments() != null && !profile.getAttachments().isEmpty()) {
            // Lưu lại các ID cần xóa
            List<Long> oldAttachmentIds = profile.getAttachments().stream()
                    .map(Attachment::getId)
                    .toList();
            
            // Xóa liên kết trong bảng trung gian
            profile.setAttachments(null);
            profileRepository.save(profile);
            
            // Xóa các attachment khỏi database
            for (Long id : oldAttachmentIds) {
                attachmentService.deleteAttachment(id);
            }
        }
        
        // Lưu file và nhận lại attachment
        Attachment attachment = attachmentService.saveFile(file);
        
        // Liên kết attachment với profile, sử dụng List.of() để tạo danh sách từ một phần tử
        attachmentService.saveAttachment(profile, List.of(attachment.getId()), true);
        
        // Cập nhật trường avatar
        profile.setAvatar(attachmentService.getImageUrl(attachment));
        
        return profileRepository.save(profile);
    }
    
    public Profile findByUser(User user) {
        return profileRepository.findByUser(user).orElse(null);
    }
    
    public Map<User, Profile> findProfilesByUsers(Set<User> users) {
        Map<User, Profile> resultMap = new HashMap<>();
        List<Profile> profiles = profileRepository.findByUserIn(users);
        
        for (Profile profile : profiles) {
            resultMap.put(profile.getUser(), profile);
        }
        
        return resultMap;
    }
}

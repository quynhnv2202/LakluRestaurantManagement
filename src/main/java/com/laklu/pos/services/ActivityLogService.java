package com.laklu.pos.services;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.entities.ActivityLog;
import com.laklu.pos.enums.TrackedResourceType;
import com.laklu.pos.repositories.ActivityLogRepository;
import com.laklu.pos.entities.Identifiable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;

    @Autowired
    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public void logActivity(Object entity, TrackedResourceType.Action action, String targetId, TrackedResourceType resourceType) {
        logActivity(entity, action, targetId, resourceType, resourceType.getMessage(action));
    }

    public void logActivity(Object entity, TrackedResourceType.Action action, String targetId, TrackedResourceType resourceType, String details) {
        ActivityLog log = new ActivityLog();
        log.setStaffId(getCurrentStaffId());
        log.setTarget(entity.getClass().getSimpleName());
        log.setAction(action);
        log.setTargetId(targetId);
        log.setDetails(details);

        activityLogRepository.save(log);
    }

    private Integer getCurrentStaffId() {
        try {
            return JwtGuard.userPrincipal().getPersitentUser().getId();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to get user ID from JWT", e);
        }
    }

    public String getEntityId(Identifiable entity) {
        return String.valueOf(entity.getId());
    }

    public List<ActivityLog> getAllActivityLogs() {
        return activityLogRepository.findAll();
    }

    public Page<ActivityLog> getAllActivityLogs(Pageable pageable) {
        return activityLogRepository.findAll(pageable);
    }

    public List<ActivityLog> getActivityLogsByUserId(Integer userId) {
        return activityLogRepository.findByStaffId(userId);
    }

    public Page<ActivityLog> getActivityLogsByUserId(Integer userId, Pageable pageable) {
        return activityLogRepository.findByStaffId(userId, pageable);
    }

    public Page<ActivityLog> getActivityLogsByTimeRange(String startTime, String endTime, Pageable pageable) {
        LocalDateTime startDateTime = LocalDateTime.parse(startTime);
        LocalDateTime endDateTime = LocalDateTime.parse(endTime);
        return activityLogRepository.findByCreatedAtBetween(startDateTime, endDateTime, pageable);
    }
}



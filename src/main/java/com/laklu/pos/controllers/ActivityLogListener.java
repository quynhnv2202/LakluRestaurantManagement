package com.laklu.pos.controllers;

    import com.laklu.pos.entities.Identifiable;
    import com.laklu.pos.enums.TrackedResourceType;
    import com.laklu.pos.services.ActivityLogService;
    import jakarta.persistence.PostPersist;
    import jakarta.persistence.PostRemove;
    import jakarta.persistence.PostUpdate;
    import jakarta.persistence.PreRemove;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.context.annotation.Lazy;
    import org.springframework.stereotype.Component;

    @Component
    public class ActivityLogListener {

        private final ActivityLogService activityLogService;

        @Autowired
        public ActivityLogListener(@Lazy ActivityLogService activityLogService) {
            this.activityLogService = activityLogService;
        }

        @PostPersist
        public void postPersist(Identifiable entity) {
            String targetId = activityLogService.getEntityId(entity);
            activityLogService.logActivity(entity, TrackedResourceType.Action.CREATE, targetId, TrackedResourceType.valueOf(entity.getClass().getSimpleName().toUpperCase()));
        }

        @PostUpdate
        public void postUpdate(Identifiable entity) {
            String targetId = activityLogService.getEntityId(entity);
            activityLogService.logActivity(entity, TrackedResourceType.Action.UPDATE, targetId, TrackedResourceType.valueOf(entity.getClass().getSimpleName().toUpperCase()));
        }

        @PreRemove
        public void preRemove(Identifiable entity) {
            String targetId = activityLogService.getEntityId(entity);
            activityLogService.logActivity(entity, TrackedResourceType.Action.DELETE, targetId, TrackedResourceType.valueOf(entity.getClass().getSimpleName().toUpperCase()));
        }

        @PostRemove
        public void postRemove(Identifiable entity) {
            // Note: In @PostRemove, you cannot directly access the entity as it has been deleted.
            // You need to save the information beforehand (e.g., in @PreRemove).
        }
    }
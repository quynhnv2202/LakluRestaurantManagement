package com.laklu.pos.controllers;

import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.response.LogResponse;
import com.laklu.pos.dataObjects.response.UserInfoResponse;
import com.laklu.pos.entities.ActivityLog;
import com.laklu.pos.services.ActivityLogService;
import com.laklu.pos.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/api/v1/activity-logs")
@Tag(name = "Activity Log Controller", description = "Quản lý log hoạt động của người dùng")
public class ActivityLogController {

    ActivityLogService activityLogService;
    UserService userService;

    @Operation(summary = "Lấy danh sách log hoạt động có phân trang", description = "API này dùng để lấy toàn bộ các log hoạt động của toàn bộ người dùng với phân trang")
    @GetMapping("/")
    public ApiResponseEntity getAllActivityLogs(Pageable pageable) throws Exception {
        Page<ActivityLog> activityLogPage = activityLogService.getAllActivityLogs(pageable);
        List<LogResponse> logResponses = mapLog(activityLogPage.getContent());
        Page<LogResponse> logResponsePage = new PageImpl<>(logResponses, pageable, activityLogPage.getTotalElements());
        return ApiResponseEntity.success(logResponsePage);
    }

    @Operation(summary = "Lấy danh sách log hoạt động theo ID người dùng có phân trang", description = "API này dùng để lấy toàn bộ các log hoạt động của một người dùng với phân trang")
    @GetMapping("/user/{userId}")
    public ApiResponseEntity getActivityLogsByUserId(@PathVariable Integer userId, Pageable pageable) throws Exception {
        Page<ActivityLog> activityLogPage = activityLogService.getActivityLogsByUserId(userId, pageable);
        List<LogResponse> logResponses = mapLog(activityLogPage.getContent());
        Page<LogResponse> logResponsePage = new PageImpl<>(logResponses, pageable, activityLogPage.getTotalElements());
        return ApiResponseEntity.success(logResponsePage);
    }

    @Operation(summary = "Lấy danh sách log hoạt động theo khoảng thời gian có phân trang", 
              description = "API này dùng để lấy toàn bộ các log hoạt động trong khoảng thời gian được chỉ định với phân trang")
    @GetMapping("/time-range")
    public ApiResponseEntity getActivityLogsByTimeRange(
            @RequestParam String startTime,
            @RequestParam String endTime,
            Pageable pageable) throws Exception {
        Page<ActivityLog> activityLogPage = activityLogService.getActivityLogsByTimeRange(startTime, endTime, pageable);
        List<LogResponse> logResponses = mapLog(activityLogPage.getContent());
        Page<LogResponse> logResponsePage = new PageImpl<>(logResponses, pageable, activityLogPage.getTotalElements());
        return ApiResponseEntity.success(logResponsePage);
    }

    private List<LogResponse> mapLog(List<ActivityLog> activityLogs) {
        List<LogResponse> logResponses = activityLogs.stream()
                .map(this::convertToLogResponse)
                .collect(Collectors.toList());
        return logResponses;
    }

    private LogResponse convertToLogResponse(ActivityLog activityLog) {
        UserInfoResponse userInfo = userService.getUserInfoById(activityLog.getStaffId());
        return new LogResponse(
                activityLog.getId(),
                activityLog.getStaffId(),
                activityLog.getAction().name(),
                activityLog.getTarget(),
                activityLog.getTargetId(),
                activityLog.getDetails(),
                activityLog.getDetails(),
                activityLog.getCreatedAt(),
                userInfo
        );
    }
}




package com.laklu.pos.dataObjects.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogResponse {
    private Integer id;
    private Integer staffId;
    private String action;
    private String target;
    private String targetId;
    private String details;
    private String message;
    private LocalDateTime createdAt;
    private UserInfoResponse userInfo;

    public LogResponse(Integer id, Integer staffId, String action, String target, String targetId, String details, String message, LocalDateTime createdAt, UserInfoResponse userInfo) {
        this.id = id;
        this.staffId = staffId;
        this.action = action;
        this.target = target;
        this.targetId = targetId;
        this.details = details;
        this.message = message;
        this.createdAt = createdAt;
        this.userInfo = userInfo;
    }
}

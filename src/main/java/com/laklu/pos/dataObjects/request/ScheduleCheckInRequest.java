package com.laklu.pos.dataObjects.request;

import lombok.Data;

@Data
public class ScheduleCheckInRequest {
    Long scheduleId;
    long expiry;
    String signature;
    String username;
    String password;
}

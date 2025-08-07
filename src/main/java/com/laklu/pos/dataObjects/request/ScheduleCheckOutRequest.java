package com.laklu.pos.dataObjects.request;

import lombok.Data;

@Data
public class ScheduleCheckOutRequest {
    Long scheduleId;
    long expiry;
    String signature;
    String username;
    String password;
}

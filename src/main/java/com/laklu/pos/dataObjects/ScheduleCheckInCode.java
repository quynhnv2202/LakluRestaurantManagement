package com.laklu.pos.dataObjects;

public class ScheduleCheckInCode implements QRCodePayload {
    private final String checkInLink;

    public ScheduleCheckInCode(String checkInLink) {
        this.checkInLink = checkInLink;
    }

    @Override
    public String getQRCodePayload() {
        return checkInLink;
    }
}

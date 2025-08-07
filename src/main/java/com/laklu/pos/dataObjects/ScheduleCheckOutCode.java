package com.laklu.pos.dataObjects;

public class ScheduleCheckOutCode implements QRCodePayload {
    private final String checkInLink;

    public ScheduleCheckOutCode(String checkInLink) {
        this.checkInLink = checkInLink;
    }

    @Override
    public String getQRCodePayload() {
        return checkInLink;
    }
}

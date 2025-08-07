package com.laklu.pos.enums;

public enum SalaryType {
    MONTHLY("Lương Tháng"),
    HOURLY("Lương Giờ"),
    SHIFTLY("Lương Theo Ca");

    private final String label;

    SalaryType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
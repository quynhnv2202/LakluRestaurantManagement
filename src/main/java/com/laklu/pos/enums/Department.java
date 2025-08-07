package com.laklu.pos.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public enum Department {
    KITCHEN("Bếp", "Nhân viên phục vuk bếp"),
    SERVICE("Phục vụ", "Nhân viên gọi món"),
    CASHIER("Thu ngân", "Nhân viên tính tiền"),
    MANAGER("Quản lý", "Nhân viên quản lý");

    Department(String label, String description) {
        this.label = label;
        this.description = description;
    }

    String label;
    String description;

}

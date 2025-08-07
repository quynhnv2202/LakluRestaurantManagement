package com.laklu.pos.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public enum OrderStatus {
    PENDING("Đang chờ", "Đơn hàng đang chờ xác nhận"),
    CONFIRMED("Đã xác nhận", "Đơn hàng đã được xác nhận"),
    COMPLETED("Đã hoàn thành", "Đơn hàng đã được hoàn thành"),
    CANCELLED("Đã hủy", "Đơn hàng đã bị huỷ"),
    ;

    OrderStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    final String label;
    final String description;
}

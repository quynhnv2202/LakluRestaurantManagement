package com.laklu.pos.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public enum OrderItemStatus {
    PENDING("Đang chờ", "Đang chờ bếp xác nhận món"),
    DOING("Đang làm", "Bếp đang làm món này"),
    COMPLETED("Đã hoàn thành", "Món ăn đã hoàn thành, sẵn sàng giao cho khách"),
    CANCELLED("Đã hủy", "Món ăn đã bị huỷ"),
    DELIVERED("Đã giao", "Món ăn đã giao cho khách")
    ;

    OrderItemStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    final String label;
    final String description;
}

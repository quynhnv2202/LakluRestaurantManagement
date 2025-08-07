package com.laklu.pos.enums;

import lombok.Getter;

@Getter
public enum PermissionGroup {
    USER("User Permissions", "USER", "Quyền liên quan đến người dùng"),
    ROLE("Role Permissions", "ROLE", "Quyền liên quan đến vai trò"),
    ATTACHMENT("Attachment Permissions", "ATTACHMENT", "Quyền liên quan đến tệp đính kèm"),
    SCHEDULE("Schedule Permissions", "SCHEDULE", "Quyền liên quan đến lịch làm việc"),
    CATEGORY("Category Permissions", "CATEGORY", "Quyền liên quan đến danh mục"),
    TABLE("Table Permissions", "TABLE", "Quyền liên quan đến bàn"),
    RESERVATION("Reservation Permissions", "RESERVATION", "Quyền liên quan đến đặt chỗ"),
    MENU("Menu Permissions", "MENU", "Quyền liên quan đến menu"),
    DISH("Dish Permissions", "DISH", "Quyền liên quan đến món ăn"),
    MENU_ITEM("Menu Item Permissions", "MENU_ITEM", "Quyền liên quan đến mục menu"),
    SALARY_RATE("Salary Rate Permissions", "SALARY_RATE", "Quyền liên quan đến mức lương"),
    ORDER("Order Permissions", "ORDER", "Quyền liên quan đến đơn món ăn"),
    PAYMENT("Quyền Thanh Toán", "PAYMENT", "Quyền liên quan đến thanh toán"),
    VOUCHER("Quyền Giảm Giá", "VOUCHER", "Quyền liên quan đến voucher"),
    PAYSLIP("Quyền Phiếu Lương", "PAYSLIP", "Quyền liên quan đến phieu luong"),
    ORDER_ITEM("Order Item Permissions", "ORDER_ITEM", "Quyền liên quan đến món ăn"),
    PROFILE("Quyền Chi Tiết Người Dùng", "PROFILE", "Quyền liên quan đến chi tiết người dùng");

    private final String label;
    private final String alias;
    private final String description;

    PermissionGroup(String label, String alias, String description) {
        this.label = label;
        this.alias = alias;
        this.description = description;
    }
}

package com.laklu.pos.enums;

public enum TrackedResourceType {
    USER("Người dùng"),
    ORDER("Đơn hàng"),
    PRODUCT("Sản phẩm"),
    CATEGORY("Danh mục"),
    PERMISSION("Quyền"),
    TABLE("Bàn"),
    MENU("Thực đơn"),
    MENUITEM("Món ăn trong thực đơn"),
    DISH("Món ăn"),
    SCHEDULE("Lịch làm việc"),
    SALARYRATE("Mức lương"),
    ROLE("Vai trò"),
    RESERVATIONTABLE("Bàn đặt"),
    ATTACHMENT("Tệp đính kèm"),
    RESERVATION("Đặt bàn"),
    PAYMENT("Thanh toán"),
    ATTENDANCE("Điểm danh");

    private final String displayName;

    TrackedResourceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public enum Action {
        CREATE("tạo"),
        UPDATE("cập nhật"),
        DELETE("xóa"),
        CANCEL("hủy"),
        PROCESS("xử lý"),
        REFUND("hoàn tiền");

        private final String actionName;

        Action(String actionName) {
            this.actionName = actionName;
        }

        public String getActionName() {
            return actionName;
        }
    }

    // Phương thức trả về thông báo theo định dạng yêu cầu
    public String getMessage(Action action) {
        return String.format("Đã %s 1 %s", action.getActionName(), this.displayName);
    }
}

package com.laklu.pos.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionCode {

    BAD_REQUEST(400, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),

    UNAUTHORIZED(401, "Chưa xác thực", HttpStatus.UNAUTHORIZED),

    FORBIDDEN(403, "Cấm truy cập", HttpStatus.FORBIDDEN),

    NOT_FOUND(404, "Không tìm thấy tài nguyên", HttpStatus.NOT_FOUND),

    METHOD_NOT_ALLOWED(405, "Phương thức không được phép", HttpStatus.METHOD_NOT_ALLOWED),

    CONFLICT(409, "Xung đột", HttpStatus.CONFLICT),

    UNPROCESSABLE_ENTITY(422, "Lỗi yêu cầu", HttpStatus.UNPROCESSABLE_ENTITY),

    INTERNAL_SERVER_ERROR(500, "Lỗi máy chủ nội bộ", HttpStatus.INTERNAL_SERVER_ERROR),

    SERVICE_UNAVAILABLE(503, "Dịch vụ không khả dụng", HttpStatus.SERVICE_UNAVAILABLE),

    INVALID_CREDENTIALS(401, "Thông tin tài khoản không khớp", HttpStatus.UNAUTHORIZED),

    INVALID_TOKEN(401, "Sai token", HttpStatus.UNAUTHORIZED);

    ExceptionCode(int code, String message, HttpStatus statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatus statusCode;
}

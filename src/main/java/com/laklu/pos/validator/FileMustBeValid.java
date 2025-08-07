package com.laklu.pos.validator;

import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class FileMustBeValid extends BaseRule {

    private final MultipartFile file;

    // Danh sách MIME Types hợp lệ
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif",
            "application/pdf",
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"
    );

    // Giới hạn dung lượng tối đa (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Override
    public String getValidateField() {
        return "file";
    }

    @Override
    public boolean isValid() {
        if (file.isEmpty()) {
            return false;
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            return false;
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return false;
        }
        return true;
    }

    @Override
    public String getMessage() {
        if (file.isEmpty()) {
            return "File không được để trống.";
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            return "Loại file không được hỗ trợ: " + file.getContentType();
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return "File vượt quá dung lượng tối đa (5MB).";
        }
        return "File không hợp lệ.";
    }
}
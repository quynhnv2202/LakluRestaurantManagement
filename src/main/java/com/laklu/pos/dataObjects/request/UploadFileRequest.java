package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadFileRequest {
    @NotNull(message = "File is required")
    private MultipartFile file;

    private String targetName;
    private Long targetId;
}

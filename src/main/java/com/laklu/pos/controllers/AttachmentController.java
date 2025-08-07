package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.AttachmentPolicy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.response.AttachmentResponse;
import com.laklu.pos.entities.Attachment;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.services.AttachmentService;
import com.laklu.pos.uiltis.Ultis;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;


@Slf4j
@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttachmentController {

    AttachmentService attachmentService;
    AttachmentPolicy attachmentPolicy;

    @Operation(summary = "Lưu file", description = "API này dùng để lưu file")
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseEntity store(@RequestParam("file") MultipartFile file) throws Exception {
        Ultis.throwUnless(attachmentPolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());
        Attachment attachment = attachmentService.saveFile(file);

        AttachmentResponse attachmentResponse = AttachmentResponse.builder()
                .id(attachment.getId())
                .mimeType(attachment.getMimeType())
                .originalName(attachment.getOriginalName())
                .randomName(attachment.getRandomName())
                .size(attachment.getSize())
                .path(attachment.getPath())
                .url(attachmentService.getImageUrl(attachment))
                .build();

        return ApiResponseEntity.success(attachmentResponse);
    }

    @Operation(summary = "Xem file", description = "API này dùng để xem file")
    @GetMapping(value = "/{filename}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE})
    public Resource show(@PathVariable String filename) throws Exception{
        Path filePath = Paths.get(AttachmentService.UPLOAD_DIRECTORY).resolve(filename);
        return new UrlResource(filePath.toUri());
    }

}

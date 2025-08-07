package com.laklu.pos.services;

import com.laklu.pos.dataObjects.response.PersistAttachmentResponse;
import com.laklu.pos.entities.Attachment;
import com.laklu.pos.entities.InteractWithAttachments;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.repositories.AttachmentRepository;
import com.laklu.pos.validator.FileMustBeValid;
import com.laklu.pos.validator.RuleValidator;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {

    //@Value("${app.base.url}")
    private String appBaseurl = "https://api.laklu.com";
    @Value("${app.base.attachment-endpoint}")
    private String attachmentEndpoint;

    public static String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/uploads";

    private final AttachmentRepository attachmentRepository;

    public Attachment saveFile(MultipartFile file) throws IOException {
        RuleValidator.validate(new FileMustBeValid(file));

        String randomName = UUID.randomUUID() + "_" + Objects.requireNonNull(file.getOriginalFilename()).replace(" ", "_");
        Path uploadPath = Paths.get(UPLOAD_DIRECTORY);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(randomName);
        file.transferTo(filePath.toFile());

        Attachment attachment = new Attachment();
        attachment.setMimeType(file.getContentType());
        attachment.setOriginalName(file.getOriginalFilename());
        attachment.setRandomName(randomName);
        attachment.setSize(file.getSize());
        attachment.setPath("/uploads/" + randomName);

        return attachmentRepository.save(attachment);
    }

    public String getImageUrl(String name) {
        Attachment attachment = attachmentRepository.findByRandomName(name).orElseThrow(NotFoundException::new);
        return appBaseurl + "/" + attachmentEndpoint + "/" + attachment.getRandomName();
    }

    public String getImageUrl(Long id) {
        Attachment attachment = attachmentRepository.findById(id).orElseThrow(NotFoundException::new);
        return appBaseurl + "/" + attachmentEndpoint + "/" + attachment.getRandomName();
    }

    public String getImageUrl(@NotNull Attachment attachment) {
        return appBaseurl + "/" + attachmentEndpoint + "/" + attachment.getRandomName();
    }

    public <T> void saveAttachment(InteractWithAttachments<T> interactWithAttachments, Long attachmentId, boolean isSync) {
        Attachment attachment = attachmentRepository.findById(attachmentId).orElseThrow(NotFoundException::new);
        attachment.setTargetId(interactWithAttachments.getId().toString());
        attachment.setTargetName(interactWithAttachments.getClass().getSimpleName());
        if(isSync) {
            interactWithAttachments.setAttachments(Set.of(attachment));
        } else {
            interactWithAttachments.addAttachment(attachment);
        }

        attachmentRepository.save(attachment);
    }

    public <T> void saveAttachment(InteractWithAttachments<T> interactWithAttachments, List<Long> attachmentIds, boolean isSync) {
        List<Attachment> attachments = attachmentRepository.findAllById(attachmentIds);
        attachments.forEach(a -> {
            a.setTargetId(interactWithAttachments.getId().toString());
            a.setTargetName(interactWithAttachments.getClass().getSimpleName());
        });
        if(isSync) {
            interactWithAttachments.setAttachments(new HashSet<>(attachments));
        } else {
            interactWithAttachments.addAttachment(attachments.toArray(new Attachment[0]));
        }
        attachmentRepository.saveAll(attachments);
    }

    public void cleanUp() {
        //TODO: Lấy ra toàn bộ ảnh trong database kiểm tra ảnh nào đang không có target id và targetName thì xóa.
    }

    public List<Attachment> findAll(List<Long> ids) {
        return attachmentRepository.findAllById(ids);
    }

    public PersistAttachmentResponse toPersistAttachmentResponse(Attachment attachment) {
        return new PersistAttachmentResponse(
                attachment.getId(),
                this.getImageUrl(attachment),
                attachment.getRandomName()
        );
    }

    public void deleteAttachment(Long id) {
        try {
            Attachment attachment = attachmentRepository.findById(id).orElse(null);
            if (attachment != null) {
                // Xóa file từ hệ thống file nếu cần
                try {
                    Path filePath = Paths.get(UPLOAD_DIRECTORY).resolve(attachment.getRandomName());
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    log.error("Không thể xóa file: " + e.getMessage());
                }
                
                // Xóa bản ghi từ database
                attachmentRepository.deleteById(id);
            }
        } catch (Exception e) {
            log.error("Lỗi khi xóa attachment: " + e.getMessage());
        }
    }

}

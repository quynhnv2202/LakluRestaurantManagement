package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAvatarRequest {
    
    @NotNull(message = "Danh sách ID của attachment không được để trống")
    private List<Long> attachmentIds;
} 
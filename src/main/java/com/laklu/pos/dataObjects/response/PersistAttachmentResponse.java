package com.laklu.pos.dataObjects.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PersistAttachmentResponse {
    private Long id;
    private String link;
    private String name;
}

package com.laklu.pos.dataObjects.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AttachmentResponse {
    private Long id;
    private String mimeType;
    private String originalName;
    private String randomName;
    private Long size;
    private String path;
    private String url;

}

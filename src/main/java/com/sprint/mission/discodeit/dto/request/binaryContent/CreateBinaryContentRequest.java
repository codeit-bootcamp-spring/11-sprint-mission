package com.sprint.mission.discodeit.dto.request.binaryContent;

import lombok.Getter;

@Getter
public class CreateBinaryContentRequest {
    private String fileName;
    private Long size;
    private String contentType;
    private byte[] bytes;
}

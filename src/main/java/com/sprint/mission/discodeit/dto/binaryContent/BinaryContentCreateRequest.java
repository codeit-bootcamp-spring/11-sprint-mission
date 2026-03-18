package com.sprint.mission.discodeit.dto.binaryContent;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class BinaryContentCreateRequest {
    private UUID userId;
    private UUID messageId;
    private String fileName;
    private byte[] content;
    private String contentType;
}

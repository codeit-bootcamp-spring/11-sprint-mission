package com.sprint.mission.discodeit.dto.binaryContent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BinaryContentCreateRequest {
    private byte[] bytes;
    private String fileName;
    private String contentType;
}
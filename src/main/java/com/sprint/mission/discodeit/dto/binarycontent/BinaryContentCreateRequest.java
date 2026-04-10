package com.sprint.mission.discodeit.dto.binarycontent;

public record BinaryContentCreateRequest(
    String fileName,
    Long size,
    String contentType,
    byte[] bytes
) {

}

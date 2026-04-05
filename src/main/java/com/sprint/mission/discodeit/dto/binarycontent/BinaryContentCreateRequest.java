package com.sprint.mission.discodeit.dto.binarycontent;

public record BinaryContentCreateRequest(
    byte[] data,
    String fileName,
    String contentType,
    long size
) {

}

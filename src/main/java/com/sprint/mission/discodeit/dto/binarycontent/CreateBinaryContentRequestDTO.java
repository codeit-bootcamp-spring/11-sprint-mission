package com.sprint.mission.discodeit.dto.binarycontent;

public record CreateBinaryContentRequestDTO(
        String fileName,
        String contentType,
        byte[] data
) {
}

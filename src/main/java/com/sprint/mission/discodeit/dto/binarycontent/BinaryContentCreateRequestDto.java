package com.sprint.mission.discodeit.dto.binarycontent;

public record BinaryContentCreateRequestDto(
        String fileName,
        byte[] data
) {
}

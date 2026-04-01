package com.sprint.mission.discodeit.dto;

public record BinaryContentCreateDto(
        byte[] data,
        String fileName,
        String contentType
) {
}

package com.sprint.mission.discodeit.dto.binarycontent;

public record BinaryContentResponse(
        byte[] bytes,
        String fileName,
        String contentType,
        long size
) {}

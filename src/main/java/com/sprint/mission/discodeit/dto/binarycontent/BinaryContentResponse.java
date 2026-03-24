package com.sprint.mission.discodeit.dto.binarycontent;

public record BinaryContentResponse(
        byte[] data,
        String fileName,
        String contentType,
        long size
) {}

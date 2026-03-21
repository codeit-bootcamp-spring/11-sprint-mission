package com.sprint.mission.discodeit.dto;

public record BinaryContentResponse(
        byte[] data,
        String fileName,
        String contentType,
        long size
) {}

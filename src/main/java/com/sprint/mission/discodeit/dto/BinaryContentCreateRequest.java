package com.sprint.mission.discodeit.dto;

public record BinaryContentCreateRequest(
        byte[] data,
        String fileName,
        String contentType,
        long size
) {}

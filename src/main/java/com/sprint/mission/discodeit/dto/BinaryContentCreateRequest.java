package com.sprint.mission.discodeit.dto;

public record BinaryContentCreateRequest(byte[] data, String fileName,
                                         Long size, String contentType) {
}

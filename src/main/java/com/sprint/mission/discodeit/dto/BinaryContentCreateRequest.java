package com.sprint.mission.discodeit.dto;

public record BinaryContentCreateRequest(byte[] bytes, String fileName,
                                         Long size, String contentType) {

}

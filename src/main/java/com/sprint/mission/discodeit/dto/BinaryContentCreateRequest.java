package com.sprint.mission.discodeit.dto;
// 프로필 이미지 업로드용 DTO

public record BinaryContentCreateRequest(
        String fileName,
        String contentType,
        byte[] bytes
) {
}

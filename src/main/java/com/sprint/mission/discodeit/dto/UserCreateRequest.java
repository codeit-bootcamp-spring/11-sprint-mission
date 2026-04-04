package com.sprint.mission.discodeit.dto;

// 유저 생성 시 필요한 파라미터를 묶어서 전달하는 DTO
public record UserCreateRequest(
        String username,
        String email,
        String password,
        byte[] bytes,
        String fileName,
        String fileType
) {
}
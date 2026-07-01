package com.sprint.mission.discodeit.dto.response;

// 로그인 성공 시 응답 Body로 내려줄 Access Token DTO
public record JwtDto(
    String accessToken
) {
}

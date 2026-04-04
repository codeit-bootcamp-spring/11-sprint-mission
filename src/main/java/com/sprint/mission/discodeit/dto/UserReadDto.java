package com.sprint.mission.discodeit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record UserReadDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String username,
        String email,
        UUID profileId, // 프로필 이미지
        @JsonProperty("online") boolean isOnline // 유저의 상태(온라인이거나 오프라인), 심화 요구사항 적용(Enum -> boolean)
        // User.Status isStatus // 유저의 상태(온라인이거나 오프라인)
) {
}

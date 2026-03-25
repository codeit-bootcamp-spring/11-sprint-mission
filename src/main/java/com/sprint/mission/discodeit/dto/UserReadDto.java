package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.User;

import java.time.Instant;
import java.util.UUID;

public record UserReadDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String name,
        String email,
        UUID profileId, // 프로필 이미지
        User.Status isStatus // 유저의 상태(온라인이거나 오프라인)
) {
}

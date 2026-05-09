package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UserStatusUpdateRequest(
        @NotNull(message = "마지막 활동 시간은 필수 입력 사항입니다.")
        Instant newLastActiveAt
) {
}
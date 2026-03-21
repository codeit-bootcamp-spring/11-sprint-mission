package com.sprint.mission.discodeit.dto.message;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record DeleteMessageRequestDTO(
        @NotNull(message = "삭제할 메세지 ID는 필수입니다.")
        UUID messageId,

        @NotNull(message = "요청 사용자 ID는 필수입니다.")
        UUID requestUserId
) {
}
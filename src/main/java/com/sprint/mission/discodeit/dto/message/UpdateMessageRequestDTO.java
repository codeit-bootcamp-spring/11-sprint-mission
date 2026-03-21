package com.sprint.mission.discodeit.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateMessageRequestDTO(
        @NotNull(message = "수정할 메세지 ID는 필수입니다.")
        UUID messageId,

        @NotNull(message = "요청 사용자 ID는 필수입니다.")
        UUID requestUserId,

        @NotBlank(message = "수정할 메세지 내용을 입력해주세요.")
        @Size(max = 2000, message = "메세지 내용은 2000자를 초과할 수 없습니다.")
        String content,

        List<UUID> attachmentIds
) {
}
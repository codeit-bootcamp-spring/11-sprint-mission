package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MessageCreatePart(
        @NotNull(message = "작성자 ID는 필수 입력 사항입니다.")
        UUID authorId,

        @NotNull(message = "채널 ID는 필수 입력 사항입니다.")
        UUID channelId,

        @NotBlank(message = "메시지 내용은 필수 입력 사항입니다.")
        @Size(max = 1000, message = "메시지 내용은 1000자 이하여야 합니다.")
        String content
) {
}
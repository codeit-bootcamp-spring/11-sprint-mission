package com.sprint.mission.discodeit.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MessageCreateRequest(
        @NotBlank(message = "메세지 내용을 입력해주세요.")
        @Size(max = 2000, message = "메세지는 2000자를 초과할 수 없습니다.")
        String content,

        @NotNull(message = "채널 ID는 필수입니다.")
        UUID channelId,

        @NotNull(message = "작성자 ID는 필수입니다.")
        UUID authorId // API 명세서 스펙 (엔티티의 userId에 해당)
) {}
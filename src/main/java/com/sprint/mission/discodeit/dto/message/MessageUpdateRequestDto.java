package com.sprint.mission.discodeit.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record MessageUpdateRequestDto(

        @NotBlank(message = "빈 메시지는 보낼 수 없습니다.")
        @Size(max = 300, message = "메시지는 300자 이하로 작성해야 합니다.")
        String contents,

        List<UUID> attachmentIds
) {
}

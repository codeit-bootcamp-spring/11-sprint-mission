package com.sprint.mission.discodeit.dto.message;

import com.sprint.mission.discodeit.entity.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record SendMessageRequestDTO(
        @NotNull(message = "메세지를 보낼 채널 ID는 필수입니다.")
        UUID channelId,

        @NotNull(message = "작성자 ID는 필수입니다.")
        UUID userId,

        @NotBlank(message = "메세지 내용을 입력해주세요.")
        @Size(max = 2000, message = "메세지 내용은 2000자를 초과할 수 없습니다.")
        String content,

        List<UUID> attachmentIds
) {
}
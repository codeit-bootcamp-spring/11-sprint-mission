package com.sprint.mission.discodeit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "수정할 Message 내용")
public record MessageUpdateRequest(

        @NotBlank(message = "빈 메시지는 보낼 수 없습니다.")
        @Size(max = 300, message = "메시지는 300자 이하로 작성해야 합니다.")
        String newContent
) {
}

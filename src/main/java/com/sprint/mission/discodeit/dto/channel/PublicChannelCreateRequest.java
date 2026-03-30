package com.sprint.mission.discodeit.dto.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Public Channel 생성 정보")
public record PublicChannelCreateRequest(

        @NotBlank(message = "이름은 필수로 기재해야 합니다.")
        String name,

        @Size(max = 200, message = "소개글은 200자 이하로 작성해야 합니다.")
        String description
) {
}

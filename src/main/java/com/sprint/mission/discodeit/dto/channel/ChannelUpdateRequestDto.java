package com.sprint.mission.discodeit.dto.channel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChannelUpdateRequestDto(

        @NotBlank(message = "이름은 필수로 기재해야 합니다.")
        String name,

        @Size(max = 200, message = "소개글은 200자 이하로 작성해야 합니다.")
        String description
) {
}

package com.sprint.mission.discodeit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "수정할 Channel 정보")
public record ChannelUpdateRequest(

        @NotBlank(message = "이름은 필수로 기재해야 합니다.")
        String newName,

        @Size(max = 500, message = "소개글은 500자 이하로 작성해야 합니다.")
        String newDescription
) {
}

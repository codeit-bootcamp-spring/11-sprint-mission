package com.sprint.mission.discodeit.dto.channeldto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PublicChanelUpdateRequest(

    @NotBlank(message = "새 채널명은 필수 입력값입니다.")
    String newName,
    @NotNull(message = "채널 설명은 null이 될 수 없습니다.")
    String newDescription
) {

}

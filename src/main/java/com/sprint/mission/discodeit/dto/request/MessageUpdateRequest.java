package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MessageUpdateRequest(
    @NotBlank(message = "메시지는 공백일 수 없습니다.")
    String newContent
) {

}

package com.sprint.mission.discodeit.dto.messagedto.request;


import jakarta.validation.constraints.NotNull;

public record MessageUpdateRequest(
    @NotNull(message = "메시지 내용은 null이 될 수 없습니다.")
    String newContent
) {


}

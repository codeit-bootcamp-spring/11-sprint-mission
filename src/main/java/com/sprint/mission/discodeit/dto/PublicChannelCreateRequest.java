package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublicChannelCreateRequest(
    @NotBlank(message = "채널 이름은 필수입니다.")
    @Size(min = 1, max = 50, message = "채널이름은 1~50자여야 합니다.")
    String name,

    @Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다.")
    String description) {

}

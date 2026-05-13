package com.sprint.mission.discodeit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "PublicChannelUpdateRequest")
public record ChannelUpdateRequest(
    @Size(min = 1, max = 50, message = "채널 이름은 1~50자여야 합니다.")
    String newName,

    @Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다.")
    String newDescription) {

}

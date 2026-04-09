package com.sprint.mission.discodeit.dto.channel;

import jakarta.validation.constraints.Size;

public record PublicChannelRequest(
    @Size(min = 2, message = "채널 이름은 최소 2자 이상이어야 합니다.")
    String name,
    String description
) {

}

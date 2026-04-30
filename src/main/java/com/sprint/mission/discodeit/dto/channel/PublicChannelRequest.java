package com.sprint.mission.discodeit.dto.channel;

import jakarta.validation.constraints.Size;

public record PublicChannelRequest(
    @Size(min = 2, max = 150, message = "채널 이름은 2~150자까지 작성가능합니다.")
    String name,

    @Size(max = 255, message = "최대 255자 까지 작성가능합니다.")
    String description
) {

}

package com.sprint.mission.discodeit.dto.channel;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record PrivateChannelRequest(
    @NotEmpty(message = "프라이빗 채널에는 최소 1명 이상의 유저가 필요합니다.")
    List<UUID> participantIds
) {

}

package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record ChannelCreatePrivateRequest(
    // 배열은 NotEmpty "[]" 까지 차단
    @NotEmpty(message = "참여자가 비어있습니다.")
    List<UUID> participantIds
) {

}

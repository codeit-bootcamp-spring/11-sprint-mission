package com.sprint.mission.discodeit.dto.channeldto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record PrivateChannelCreateRequest(
    @NotNull(message = "참여자가 존재해야 채널을 생성할 수 있습니다.")
    List<UUID> participantIds

) {

}

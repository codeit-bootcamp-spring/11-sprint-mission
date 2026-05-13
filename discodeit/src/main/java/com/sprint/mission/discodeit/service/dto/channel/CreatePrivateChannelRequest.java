package com.sprint.mission.discodeit.service.dto.channel;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record CreatePrivateChannelRequest(
        @NotEmpty List<UUID> participantIds
) {
}

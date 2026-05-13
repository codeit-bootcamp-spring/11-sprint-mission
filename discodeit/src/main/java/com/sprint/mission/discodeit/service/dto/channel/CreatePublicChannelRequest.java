package com.sprint.mission.discodeit.service.dto.channel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePublicChannelRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description
) {
}

package com.sprint.mission.discodeit.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChannelUpdateApiRequest(
        @NotBlank @Size(max = 100) String newName,
        @Size(max = 500) String newDescription
) {
}

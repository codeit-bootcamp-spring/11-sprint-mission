package com.sprint.mission.discodeit.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record MessageUpdateApiRequest(
        @NotBlank String newContent
) {
}

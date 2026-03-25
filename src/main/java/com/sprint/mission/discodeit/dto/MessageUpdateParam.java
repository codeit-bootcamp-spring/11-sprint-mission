package com.sprint.mission.discodeit.dto;

import java.util.UUID;

public record MessageUpdateParam(
        UUID id,
        MessageUpdateRequest request
) {
}
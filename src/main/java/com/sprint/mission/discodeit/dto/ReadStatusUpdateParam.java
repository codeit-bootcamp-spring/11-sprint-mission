package com.sprint.mission.discodeit.dto;

import java.util.UUID;

public record ReadStatusUpdateParam(
        UUID id,
        ReadStatusUpdateRequest request
) {
}
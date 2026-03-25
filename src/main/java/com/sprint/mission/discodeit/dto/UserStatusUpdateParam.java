package com.sprint.mission.discodeit.dto;

import java.util.UUID;

public record UserStatusUpdateParam(
        UUID id,
        UserStatusUpdateRequest request
) {
}
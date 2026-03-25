package com.sprint.mission.discodeit.dto;

import java.util.UUID;

public record UserUpdateParam(
        UUID id,
        UserUpdateRequest request
) {
}

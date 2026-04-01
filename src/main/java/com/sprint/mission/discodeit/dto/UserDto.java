package com.sprint.mission.discodeit.dto;
// password 제외
// online 포함

import java.time.Instant;
import java.util.UUID;

public record UserDto (
        UUID id,
        String userName,
        String email,
        String statusMessage,
        UUID profileId,
        Boolean online,
        Instant createdAt,
        Instant updatedAt
){
}

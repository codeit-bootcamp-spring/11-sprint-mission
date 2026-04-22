package com.sprint.mission.discodeit.dto.user;

import java.time.Instant;
import java.util.UUID;

@Deprecated
public record UserResponse(
    UUID id,
    Instant createdAt,
    Instant updatedAt,
    String username,
    String email,
    String password,
    UUID profileId
) {

}

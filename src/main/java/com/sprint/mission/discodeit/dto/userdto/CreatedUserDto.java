package com.sprint.mission.discodeit.dto.userdto;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record CreatedUserDto(
    UUID id,
    Instant createdAt,
    Instant updatedAt,
    String username,
    String email,
    String password,
    UUID profileId
    
) {

}

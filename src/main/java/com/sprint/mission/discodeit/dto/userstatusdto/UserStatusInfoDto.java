package com.sprint.mission.discodeit.dto.userstatusdto;

import java.time.Instant;
import java.util.UUID;

public record UserStatusInfoDto(

    UUID id,
    Instant createdAt,
    Instant updatedAt,
    UUID userId,
    Instant lastActiveAt,
    boolean online

) {

}

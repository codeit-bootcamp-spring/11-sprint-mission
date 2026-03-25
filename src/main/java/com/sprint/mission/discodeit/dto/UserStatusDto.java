package com.sprint.mission.discodeit.dto;

import java.time.Instant;
import java.util.UUID;

public record UserStatusDto(UUID id, UUID userid, Instant createdAt,
                            Instant updatedAt, boolean isOnline) {
}

package com.sprint.mission.discodeit.dto;

import java.time.Instant;
import java.util.UUID;

public record UserDto(UUID id, String name, String email, UUID profileId,
                      Instant createdAt, Instant updatedAt, boolean isOnline) {
}

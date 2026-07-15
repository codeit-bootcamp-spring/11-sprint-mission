package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.security.authority.UserRole;

import java.util.UUID;

public record RoleUpdatedEvent(
        UUID userId,
        UserRole previousRole,
        UserRole newRole
) {
}

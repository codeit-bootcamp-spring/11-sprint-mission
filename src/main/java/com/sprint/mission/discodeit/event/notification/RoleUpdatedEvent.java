package com.sprint.mission.discodeit.event.notification;

import com.sprint.mission.discodeit.security.Role;
import java.util.UUID;

public record RoleUpdatedEvent(
    UUID userId,
    Role oldRole,
    Role newRole
) {

}
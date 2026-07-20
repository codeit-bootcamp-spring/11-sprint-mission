package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.User.Role;
import java.util.UUID;

public record RoleUpdatedEvent(
    UUID userId,
    Role beforeRole,
    Role newRole
) {

}

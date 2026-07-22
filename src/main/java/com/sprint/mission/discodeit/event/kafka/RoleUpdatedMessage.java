package com.sprint.mission.discodeit.event.kafka;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import java.util.UUID;

public record RoleUpdatedMessage(
    UUID userId,
    Role oldRole,
    Role newRole
) {

  public static RoleUpdatedMessage from(RoleUpdatedEvent event) {
    return new RoleUpdatedMessage(event.userId(), event.oldRole(), event.newRole());
  }
}

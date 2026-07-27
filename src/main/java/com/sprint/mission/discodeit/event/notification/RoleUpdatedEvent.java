package com.sprint.mission.discodeit.event.notification;

import com.sprint.mission.discodeit.entity.User.Role;
import com.sprint.mission.discodeit.event.UpdatedEvent;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class RoleUpdatedEvent extends UpdatedEvent<Role> {

  private final UUID userId;

  public RoleUpdatedEvent(UUID userId, Role beforeRole, Role newRole, Instant updatedAt) {
    super(beforeRole, newRole, updatedAt);
    this.userId = userId;
  }
}

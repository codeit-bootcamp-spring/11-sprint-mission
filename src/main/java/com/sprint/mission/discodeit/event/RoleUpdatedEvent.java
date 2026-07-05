package com.sprint.mission.discodeit.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoleUpdatedEvent {

  private final UUID userId;
  private final String oldRole;
  private final String newRole;
}

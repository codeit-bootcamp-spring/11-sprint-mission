package com.sprint.mission.discodeit.event;

import java.util.UUID;
import lombok.Getter;

@Getter
public class PasswordChangeEvent {

  private final UUID userId;

  public PasswordChangeEvent(UUID userId) {
    this.userId = userId;
  }
}

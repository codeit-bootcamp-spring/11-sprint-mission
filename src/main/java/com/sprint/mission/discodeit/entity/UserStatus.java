package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus extends Entity {

  private final UUID userId;
  private Instant lastActiveAt;

  public UserStatus(UUID userId) {
    this.userId = userId;
    lastActiveAt = Instant.now();
  }


  public void updateLastActiveAt(Instant lastActiveAt) {
    this.lastActiveAt = lastActiveAt;
    super.updateUpdatedAt();
  }

  public boolean isOnline() {

    Instant now = Instant.now();

    if (getLastActiveAt().isAfter(now.minusSeconds(300))) {

      return true;
    } else {
      return false;
    }


  }


}

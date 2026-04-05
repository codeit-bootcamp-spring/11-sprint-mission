package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Getter
public class UserStatus implements Serializable {

  private UUID id;
  private UUID userId;
  private Instant createdAt;
  private Instant updatedAt;
  private Instant lastActiveAt;

  public UserStatus(UUID userId) {
    this.id = UUID.randomUUID();
    this.userId = userId;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
    this.lastActiveAt = Instant.now();
  }

  public boolean isOnline() {
    return this.updatedAt.isAfter(Instant.now().minus(5,
        ChronoUnit.MINUTES));
  }

  public void update(Instant newLastActiveAt) {
    this.lastActiveAt = newLastActiveAt != null ? newLastActiveAt : Instant.now();
    this.updatedAt = Instant.now();
  }
}

package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ReadStatus implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private UUID userId;
  private UUID channelId;
  private Instant createdAt;
  private Instant updatedAt;
  private Instant lastReadAt;

  public ReadStatus(UUID userId, UUID channelId, Instant lastReadAt) {
    this.id = UUID.randomUUID();
    this.userId = userId;
    this.channelId = channelId;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
    this.lastReadAt = (lastReadAt != null) ? lastReadAt : Instant.now();
  }

  public void update(Instant newLastReadAt) {
    if (newLastReadAt != null) {
      this.lastReadAt = newLastReadAt;
    }
    this.updatedAt = Instant.now();
  }
}

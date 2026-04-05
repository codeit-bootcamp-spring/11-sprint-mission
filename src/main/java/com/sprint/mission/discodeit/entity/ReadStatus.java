package com.sprint.mission.discodeit.entity;


import java.time.Instant;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ReadStatus extends Entity {

  private final UUID channelId;
  private final UUID userId;
  private Instant lastReadAt;

  public ReadStatus(UUID userId, UUID channelId, Instant lastReadAt) {
    this.userId = userId;
    this.channelId = channelId;
    this.lastReadAt = lastReadAt;
  }

  public Instant updateLastReadAt(Instant lastReadAt) {
    this.lastReadAt = lastReadAt;
    updateUpdatedAt();
    return lastReadAt;
  }


}

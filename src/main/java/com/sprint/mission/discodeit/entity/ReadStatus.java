package com.sprint.mission.discodeit.entity;

import java.util.UUID;
import lombok.Getter;

@Getter
public class ReadStatus extends BaseEntity {

  private final UUID userId;
  private final UUID channelId;

  public ReadStatus(UUID userId, UUID channelId) {
    this.userId = userId;
    this.channelId = channelId;
  }
}

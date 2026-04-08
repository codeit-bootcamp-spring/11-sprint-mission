package com.sprint.mission.discodeit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ReadStatus extends BaseEntity {

  private final UUID userId;
  private final UUID channelId;

  @Builder.Default
  private Instant lastReadAt = Instant.now();

  // updatedAt을 현재 시간으로 갱신
  public void update(Instant newLastReadAt) {
    if (newLastReadAt != null && !newLastReadAt.equals(this.lastReadAt)) {
      this.lastReadAt = newLastReadAt;
      super.timeUpdate();
    }
  }
}
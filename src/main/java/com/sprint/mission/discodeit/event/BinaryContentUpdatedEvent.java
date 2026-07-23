package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.sse.BinaryContentStatusDto;
import java.time.Instant;

public class BinaryContentUpdatedEvent extends UpdatedEvent<BinaryContentStatusDto> {

  private final java.util.UUID uploaderId;

  public BinaryContentUpdatedEvent(BinaryContentStatusDto data, Instant updatedAt,
      java.util.UUID uploaderId) {
    super(data, updatedAt);
    this.uploaderId = uploaderId;
  }

  public java.util.UUID getUploaderId() {
    return uploaderId;
  }
}
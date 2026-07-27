package com.sprint.mission.discodeit.event.binarycontent;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.event.UpdatedEvent;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class BinaryContentUpdatedEvent extends UpdatedEvent<BinaryContentDto> {

  private final UUID channelId;
  private final UUID receiverId;

  public BinaryContentUpdatedEvent(
      BinaryContentDto from,
      BinaryContentDto to,
      Instant updatedAt,
      UUID channelId,
      UUID receiverId) {
    super(from, to, updatedAt);
    this.channelId = channelId;
    this.receiverId = receiverId;
  }

}

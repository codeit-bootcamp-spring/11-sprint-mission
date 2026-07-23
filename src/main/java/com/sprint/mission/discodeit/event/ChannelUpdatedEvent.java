package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import java.time.Instant;

public class ChannelUpdatedEvent extends UpdatedEvent<ChannelDto> {

  public ChannelUpdatedEvent(ChannelDto data, Instant updatedAt) {
    super(data, updatedAt);
  }
}

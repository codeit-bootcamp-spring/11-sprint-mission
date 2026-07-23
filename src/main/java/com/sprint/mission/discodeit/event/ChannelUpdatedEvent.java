package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import java.time.Instant;

public class ChannelUpdatedEvent extends UpdatedEvent<ChannelDto> {

  public ChannelUpdatedEvent(ChannelDto data, Instant updatedAt) {
    super(data, updatedAt);
  }
}
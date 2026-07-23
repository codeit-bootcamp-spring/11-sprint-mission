package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import java.time.Instant;

public class ChannelCreatedEvent extends CreatedEvent<ChannelDto> {

  public ChannelCreatedEvent(ChannelDto data, Instant createdAt) {
    super(data, createdAt);
  }
}
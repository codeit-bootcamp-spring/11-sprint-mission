package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import java.time.Instant;

public class ChannelDeletedEvent extends DeletedEvent<ChannelDto> {

  public ChannelDeletedEvent(ChannelDto data, Instant deletedAt) {
    super(data, deletedAt);
  }
}
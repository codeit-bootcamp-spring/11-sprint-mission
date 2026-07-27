package com.sprint.mission.discodeit.event.notification;

import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.event.CreatedEvent;
import java.time.Instant;

public class MessageCreatedEvent extends CreatedEvent<MessageDto> {

  public MessageCreatedEvent(MessageDto data, Instant createdAt) {
    super(data, createdAt);
  }

}

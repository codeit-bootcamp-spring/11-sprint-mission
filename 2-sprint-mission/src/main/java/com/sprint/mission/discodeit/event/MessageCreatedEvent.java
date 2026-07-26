package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.MessageDto;
import java.util.UUID;

public record MessageCreatedEvent(
    String channelName,
    MessageDto.Response message
) {

  public UUID channelId() {
    return message.channelId();
  }

  public UUID authorId() {
    return message.author().id();
  }

  public String authorName() {
    return message.author().username();
  }

  public String content() {
    return message.content();
  }
}
package com.sprint.mission.discodeit.event.kafka;

import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import java.util.UUID;

public record MessageCreatedMessage(
    UUID channelId,
    UUID authorId,
    String authorName,
    String content,
    String channelName
) {

  public static MessageCreatedMessage from(MessageCreatedEvent event) {
    return new MessageCreatedMessage(
        event.channelId(),
        event.authorId(),
        event.authorName(),
        event.content(),
        event.channelName()
    );
  }
}

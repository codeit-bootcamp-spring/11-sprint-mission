package com.sprint.mission.discodeit.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageCreatedEvent {

  private final UUID messageId;
  private final UUID channelId;
  private final UUID authorId;
  private final String channelName;
  private final String content;
}

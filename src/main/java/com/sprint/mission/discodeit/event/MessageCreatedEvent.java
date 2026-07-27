package com.sprint.mission.discodeit.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreatedEvent {

  private UUID messageId;
  private UUID channelId;
  private UUID authorId;
  private String channelName;
  private String content;
}

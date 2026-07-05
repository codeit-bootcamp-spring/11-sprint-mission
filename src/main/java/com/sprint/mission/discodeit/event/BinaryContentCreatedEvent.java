package com.sprint.mission.discodeit.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BinaryContentCreatedEvent {

  private final UUID binaryContentId;
  private final byte[] bytes;
}

package com.sprint.mission.discodeit.event.sse;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import java.util.Set;
import java.util.UUID;

public record BinaryContentStatusUpdatedEvent(
    Set<UUID> receiverIds,
    BinaryContentResponse binaryContent
) {

  public static final String TOPIC = "discodeit.BinaryContentStatusUpdatedEvent";
}
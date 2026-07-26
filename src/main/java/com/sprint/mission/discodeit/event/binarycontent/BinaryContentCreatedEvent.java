package com.sprint.mission.discodeit.event.binarycontent;

import java.util.Set;
import java.util.UUID;

public record BinaryContentCreatedEvent(
    UUID binaryContentId,
    byte[] bytes,
    Set<UUID> receiverIds
) {

}
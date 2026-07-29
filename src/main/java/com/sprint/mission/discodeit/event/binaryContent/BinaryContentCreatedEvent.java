package com.sprint.mission.discodeit.event.binaryContent;

import java.util.UUID;

public record BinaryContentCreatedEvent(
    UUID binaryContentId,
    byte[] bytes,
    UUID ownerId
) {

}

package com.sprint.mission.discodeit.event.binaryContent;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentDto;
import java.util.UUID;

public record BinaryContentUpdatedEvent(
    BinaryContentDto data,
    UUID ownerId
) {

}

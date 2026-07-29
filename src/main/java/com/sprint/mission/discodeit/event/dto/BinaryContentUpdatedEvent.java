package com.sprint.mission.discodeit.event.dto;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import java.time.Instant;
import java.util.UUID;

public record BinaryContentUpdatedEvent(
    BinaryContentDto data,
    UUID uploaderId,
    Instant createdAt
) {

}

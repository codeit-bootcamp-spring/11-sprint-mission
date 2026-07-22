package com.sprint.mission.discodeit.dto.binarycontentdto.event;

import com.sprint.mission.discodeit.entity.BinaryContent;
import java.util.UUID;

public record BinaryContentCreatedEvent(
    UUID binaryContentId,
    BinaryContent binaryContent,
    byte[] data
) {

}
package com.sprint.mission.discodeit.dto.sse;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import java.util.UUID;

public record BinaryContentStatusDto(
    UUID binaryContentId,
    BinaryContentStatus status
) {

}
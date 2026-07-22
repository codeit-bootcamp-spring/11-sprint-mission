package com.sprint.mission.discodeit.dto.binarycontentdto;

import com.sprint.mission.discodeit.entity.BinaryContent.BinaryContentStatus;
import java.util.UUID;

public record BinaryContentDto(

    UUID id,
    String fileName,
    Long size,
    String contentType,
    BinaryContentStatus status
) {

}

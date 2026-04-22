package com.sprint.mission.discodeit.dto.binarycontentdto;

import java.time.Instant;
import java.util.UUID;

public record BinaryContentDto(

    UUID id,
    String fileName,
    Long size,
    String contentType,
    byte[] bytes

) {

}

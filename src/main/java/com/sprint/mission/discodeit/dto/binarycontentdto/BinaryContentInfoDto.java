package com.sprint.mission.discodeit.dto.binarycontentdto;

import java.time.Instant;
import java.util.UUID;

public record BinaryContentInfoDto(

    UUID id,
    Instant createdAt,
    String fileName,
    Long size,
    String contentType,
    byte[] bytes

) {

}

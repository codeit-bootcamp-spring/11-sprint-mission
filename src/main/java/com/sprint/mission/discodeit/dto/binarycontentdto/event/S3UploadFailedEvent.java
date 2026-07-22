package com.sprint.mission.discodeit.dto.binarycontentdto.event;

import java.util.UUID;

public record S3UploadFailedEvent(
    String taskName,
    String requestId,
    UUID binaryContentId,
    String errorMessage
) {

}

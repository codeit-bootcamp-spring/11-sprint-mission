package com.sprint.mission.discodeit.event.notification;

import java.util.UUID;

public record S3UploadFailedEvent(
    UUID binaryContentId,
    Exception e
) {

}

package com.sprint.mission.discodeit.event.binarycontent;

import java.util.UUID;

public record S3UploadFailedEvent(
    String requestId,
    UUID binaryContentId,
    String errorMessage
) {

  public static final String TOPIC = "discodeit.S3UploadFailedEvent";
}
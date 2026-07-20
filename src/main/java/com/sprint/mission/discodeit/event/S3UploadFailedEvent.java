package com.sprint.mission.discodeit.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class S3UploadFailedEvent {

  private final String requestId;
  private final UUID binaryContentId;
  private final String errorMessage;
}
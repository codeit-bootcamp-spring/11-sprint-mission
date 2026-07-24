package com.sprint.mission.discodeit.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

public class NotificationDto {

  @Builder
  public record Response(
      UUID id,
      Instant createdAt,
      String title,
      String content,
      UUID receiverId
  ) {

  }
}
package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

public class ReadStatusDto {

  public record CreateRequest(
      @NotNull(message = "유저 ID는 필수 항목입니다.")
      UUID userId,

      @NotNull(message = "채널 ID는 필수 항목입니다.")
      UUID channelId,

      Instant lastReadAt
  ) {

    // DTO -> Entity
    public ReadStatus toEntity(User user, Channel channel) {
      return ReadStatus.builder()
          .user(user)
          .channel(channel)
          .lastReadAt(this.lastReadAt)
          .build();
    }
  }

  public record UpdateRequest(
      Instant newLastReadAt
  ) {

  }

  @Builder
  public record Response(
      UUID id,
      UUID userId,
      UUID channelId,
      Instant lastReadAt
  ) {

  }
}
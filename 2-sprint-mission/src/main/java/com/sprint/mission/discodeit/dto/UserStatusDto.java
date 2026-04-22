package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

public class UserStatusDto {

  public record CreateRequest(
      @NotNull(message = "유저 ID는 필수 항목입니다.")
      UUID userId,

      Instant lastActiveAt
  ) {

    public UserStatus toEntity() {
      return UserStatus.builder()
          .lastActiveAt(this.lastActiveAt)
          .build();
    }
  }

  public record UpdateRequest(
      Instant newLastActiveAt
  ) {

  }

  @Builder
  public record Response(
      UUID id,
      UUID userId,
      Instant lastActiveAt
  ) {

  }
}
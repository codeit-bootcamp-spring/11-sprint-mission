package com.sprint.mission.discodeit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record UserDto(
    UUID id,
    UUID profileId,
    String username,
    String email,

    @JsonProperty("online")
    @Schema(type = "boolean")
    Boolean online,

    Instant createdAt,
    Instant updatedAt
) {

  public static UserDto from(User user, UserStatus status) {
    return new UserDto(
        user.getId(),
        user.getProfileId(),
        user.getUsername(),
        user.getEmail(),
        Optional.ofNullable(status).map(UserStatus::getOnlineStatus).orElse(false),
        Optional.ofNullable(status).map(UserStatus::getCreatedAt).orElse(null),
        Optional.ofNullable(status).map(UserStatus::getUpdatedAt).orElse(null)
    );
  }
}

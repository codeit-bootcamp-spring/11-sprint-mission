package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.Optional;
import java.util.UUID;

public record UserDto(
    UUID id,
    String username,
    String email,
    BinaryContentDto profile,
    Boolean online
) {

  public static UserDto from(User user, UserStatus status) {
    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        Optional.ofNullable(user.getProfile())
            .map(BinaryContentDto::from).orElse(null),
        Optional.ofNullable(status)
            .map(UserStatus::getOnlineStatus).orElse(false)
    );
  }
}

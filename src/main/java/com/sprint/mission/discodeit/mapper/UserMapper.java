package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

  private final BinaryContentMapper binaryContentMapper;

  public UserDto toDto(User user, UserStatus status) {
    if (user == null) {
      return null;
    }

    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        Optional.ofNullable(user.getProfile())
            .map(binaryContentMapper::toDto).orElse(null),
        Optional.ofNullable(status)
            .map(UserStatus::getOnlineStatus).orElse(false)
    );
  }
}

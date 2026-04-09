package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

  private final BinaryContentMapper binaryContentMapper;

  public UserDto toDto(User user) {
    BinaryContentDto profileDto = null;

    if (user.getProfile() != null) {
      profileDto = binaryContentMapper.toDto(user.getProfile());
    }

    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        profileDto,
        user.getStatus() != null && user.getStatus().isOnline()
    );
  }
}

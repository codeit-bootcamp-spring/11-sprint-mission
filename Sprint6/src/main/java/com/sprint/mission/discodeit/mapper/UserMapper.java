package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.User;

public class UserMapper {

  public static UserDto toDto(User user) {
    if (user == null) {
      return null;
    }
    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        null, // BinaryContentDto (나중에 구현)
        false // 온라인 상태 기본값
    );
  }
}
package com.sprint.mission.discodeit.dto.data;

import java.util.UUID;
import com.sprint.mission.discodeit.entity.Role;

public record UserDto(
    UUID id,
    String username,
    String email,
    BinaryContentDto profile,
    Boolean online,
    Role role
) {
  public UserDto(
      UUID id,
      String username,
      String email,
      BinaryContentDto profile,
      Boolean online
  ) {
    // 기존 테스트 코드 호환을 위해 기본 USER 역할을 채움
    this(id, username, email, profile, online, Role.USER);
  }
}

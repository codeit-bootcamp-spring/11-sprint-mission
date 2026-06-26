package com.sprint.mission.discodeit.mapper;

import org.springframework.stereotype.Component;

@Component
public class UserStatusMapper {

  public UserStatusDto toDto(UserStatus userStatus) {
    if (userStatus == null) {
      return null;
    }

    return new UserStatusDto(
        userStatus.getId(),
        userStatus.getUser() != null ? userStatus.getUser().getId() : null,
        userStatus.getLastActiveAt()
    );
  }
}

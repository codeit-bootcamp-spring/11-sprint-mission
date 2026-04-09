package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class UserStatusMapper {

  public UserStatusDto toDto(UserStatus status) {
    if (status == null) {
      return null;
    }

    return new UserStatusDto(
        status.getId(),
        status.getUser().getId(),
        status.getLastActiveAt()
    );
  }

}

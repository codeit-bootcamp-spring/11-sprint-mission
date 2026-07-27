package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.user.UserDto;
import java.time.Instant;

public class UserDeletedEvent extends DeletedEvent<UserDto> {

  public UserDeletedEvent(UserDto data, Instant deletedAt) {
    super(data, deletedAt);
  }
}

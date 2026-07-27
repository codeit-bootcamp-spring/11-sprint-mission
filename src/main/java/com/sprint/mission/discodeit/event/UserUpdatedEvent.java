package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.user.UserDto;
import java.time.Instant;

public class UserUpdatedEvent extends UpdatedEvent<UserDto> {

  public UserUpdatedEvent(UserDto data, Instant updatedAt) {
    super(data, updatedAt);
  }
}

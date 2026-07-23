package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.response.UserDto;
import java.time.Instant;

public class UserCreatedEvent extends CreatedEvent<UserDto> {

  public UserCreatedEvent(UserDto data, Instant createdAt) {
    super(data, createdAt);
  }
}
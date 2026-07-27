package com.sprint.mission.discodeit.event.user;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.event.CreatedEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class UserCreatedEvent extends CreatedEvent<UserDto> {

  public UserCreatedEvent(UserDto data, Instant createdAt) {
    super(data, createdAt);
  }

}

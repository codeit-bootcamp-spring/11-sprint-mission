package com.sprint.mission.discodeit.event.user;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.event.UpdatedEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class UserUpdatedEvent extends UpdatedEvent<UserDto> {

  public UserUpdatedEvent(UserDto from, UserDto to, Instant updatedAt) {
    super(from, to, updatedAt);
  }

}

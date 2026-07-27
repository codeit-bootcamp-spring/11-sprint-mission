package com.sprint.mission.discodeit.event.user;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.event.DeletedEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class UserDeletedEvent extends DeletedEvent<UserDto> {

  public UserDeletedEvent(UserDto data, Instant deletedAt) {
    super(data, deletedAt);
  }

}

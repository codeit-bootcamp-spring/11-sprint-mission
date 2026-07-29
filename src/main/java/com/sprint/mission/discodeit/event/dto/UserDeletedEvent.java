package com.sprint.mission.discodeit.event.dto;

import com.sprint.mission.discodeit.dto.user.UserDto;
import java.time.Instant;

public record UserDeletedEvent(
    UserDto data,
    Instant createdAt
) {

}

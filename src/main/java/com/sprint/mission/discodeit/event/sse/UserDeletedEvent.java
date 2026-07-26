package com.sprint.mission.discodeit.event.sse;

import com.sprint.mission.discodeit.dto.user.UserResponse;

public record UserDeletedEvent(
    UserResponse user
) {

  public static final String TOPIC = "discodeit.UserDeletedEvent";
}
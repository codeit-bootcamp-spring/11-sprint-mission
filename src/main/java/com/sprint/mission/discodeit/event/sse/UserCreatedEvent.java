package com.sprint.mission.discodeit.event.sse;

import com.sprint.mission.discodeit.dto.user.UserResponse;

public record UserCreatedEvent(
    UserResponse user
) {

  public static final String TOPIC = "discodeit.UserCreatedEvent";
}
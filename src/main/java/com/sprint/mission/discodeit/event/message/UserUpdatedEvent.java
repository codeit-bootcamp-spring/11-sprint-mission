package com.sprint.mission.discodeit.event.message;

import com.sprint.mission.discodeit.dto.data.UserDto;
import lombok.Getter;

import java.time.Instant;

@Getter
public class UserUpdatedEvent {

    private final UserDto data;
    private final Instant updatedAt;

    public UserUpdatedEvent(UserDto data, Instant updatedAt) {
        this.data = data;
        this.updatedAt = updatedAt;
    }
}

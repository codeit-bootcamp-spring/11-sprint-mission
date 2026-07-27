package com.sprint.mission.discodeit.dto.user;

import java.util.UUID;

public record UserOnlineStatusChangedEvent(
    UUID userId,
    boolean online
) {

}

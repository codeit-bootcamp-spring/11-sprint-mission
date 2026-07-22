package com.sprint.mission.discodeit.event;

import java.util.UUID;

public record MessageCreatedEvent(
    UUID channelId,
    UUID authorId,
    String authorName,
    String content,
    String channelName
) {}

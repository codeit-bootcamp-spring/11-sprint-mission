package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class ReadStatus {
    private final UUID id;
    private final UUID channelId;
    private final UUID userId;
    private final Instant createdAt;
    private final Instant updatedAt;
}

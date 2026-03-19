package com.sprint.mission.discodeit.dto.readStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReadStatusUpdateRequest {
    private UUID userId;
    private UUID channelId;
    private Instant lastMessageReadAt;
}

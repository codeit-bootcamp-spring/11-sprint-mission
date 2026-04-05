package com.sprint.mission.discodeit.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReadStatusCreateRequestDto {
    private UUID userId;
    private UUID channelId;
    private Instant lastReadAt;
    private boolean isRead;
}

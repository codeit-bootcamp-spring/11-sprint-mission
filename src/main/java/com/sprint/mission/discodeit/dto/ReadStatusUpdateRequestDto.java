package com.sprint.mission.discodeit.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReadStatusUpdateRequestDto {
    private UUID id;
    private Instant lastReadAt;
    private boolean isRead;
}

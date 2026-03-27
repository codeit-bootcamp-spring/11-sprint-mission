package com.sprint.mission.discodeit.dto.readStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReadStatusUpdateRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID channelId;

    @NotNull
    private Instant lastMessageReadAt;
}

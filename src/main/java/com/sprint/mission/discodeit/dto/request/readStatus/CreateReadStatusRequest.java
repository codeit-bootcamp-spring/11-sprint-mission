package com.sprint.mission.discodeit.dto.request.readStatus;

import lombok.Getter;

import java.util.UUID;

@Getter
public class CreateReadStatusRequest {
    private UUID userId;
    private UUID channelId;
}

package com.sprint.mission.discodeit.dto.request.readStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CreateReadStatusRequest {
    private UUID userId;
    private UUID channelId;
}

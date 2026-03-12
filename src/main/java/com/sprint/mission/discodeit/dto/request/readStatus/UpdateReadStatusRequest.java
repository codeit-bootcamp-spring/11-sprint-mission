package com.sprint.mission.discodeit.dto.request.readStatus;

import lombok.Getter;

import java.time.Instant;

@Getter
public class UpdateReadStatusRequest {
    private Instant lastReadAt;
}

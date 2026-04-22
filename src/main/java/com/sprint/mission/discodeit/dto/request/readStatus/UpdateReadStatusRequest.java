package com.sprint.mission.discodeit.dto.request.readStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class UpdateReadStatusRequest {
    private Instant lastReadAt;
}

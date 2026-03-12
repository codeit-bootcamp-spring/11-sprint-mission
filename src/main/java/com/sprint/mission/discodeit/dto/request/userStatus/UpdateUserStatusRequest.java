package com.sprint.mission.discodeit.dto.request.userStatus;

import lombok.Getter;

import java.time.Instant;

@Getter
public class UpdateUserStatusRequest {
    private Instant lastActiveAt;
}

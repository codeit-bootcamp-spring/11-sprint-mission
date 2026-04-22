package com.sprint.mission.discodeit.dto.request.userStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class UpdateUserStatusRequest {
    private Instant lastActiveAt;
}

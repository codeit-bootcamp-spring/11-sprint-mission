package com.sprint.mission.discodeit.dto.userStatus;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserStatusUpdateRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private Instant lastOnlineAt;
}

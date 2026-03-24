package com.sprint.mission.discodeit.dto.userstatusdto;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.UUID;

public record UserStatusInfoDto(

        UUID userId,
        UserStatus.Status status

) {
}

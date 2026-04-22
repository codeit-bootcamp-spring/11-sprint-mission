package com.sprint.mission.discodeit.dto.userstatusdto;

import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.UUID;

public record CreateUserStatusDto(

    User user,
    Instant lastActiveAt

) {

}

package com.sprint.mission.discodeit.dto.userdto.event;

import com.sprint.mission.discodeit.entity.User;
import java.util.UUID;

public record RoleUpdatedEvent(

    UUID userId,
    User.Role previousRole,
    User.Role newRole

) {

}

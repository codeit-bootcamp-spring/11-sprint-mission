package com.sprint.mission.discodeit.dto.userdto.request;

import com.sprint.mission.discodeit.entity.User;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RoleUpdateRequest(

    @NotNull
    UUID userId,

    @NotNull
    User.Role newRole

) {

}

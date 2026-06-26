package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.Role;

import java.util.UUID;

public record UserRoleUpdatedRequest (
    UUID userId,
    Role newRole
) {

}

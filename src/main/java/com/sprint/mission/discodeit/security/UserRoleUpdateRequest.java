package com.sprint.mission.discodeit.security;

import java.util.UUID;

public record UserRoleUpdateRequest(
    UUID userId,
    Role newRole
) {

}
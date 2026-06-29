package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.security.authority.UserRole;

import java.util.UUID;

public record UserRoleUpdateRequest(
        UUID userId,
        UserRole newRole
) {
}

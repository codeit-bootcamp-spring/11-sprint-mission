package com.sprint.mission.discodeit.dto.user;

import java.util.UUID;

public record UserInfoDto(
        UUID id,
        String name,
        String email,
        UUID profileImage,
        boolean isOnline
) {
}

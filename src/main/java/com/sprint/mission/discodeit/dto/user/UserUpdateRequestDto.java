package com.sprint.mission.discodeit.dto.user;

import java.util.UUID;

public record UserUpdateRequestDto(
        String name,
        String email,
        String password,
        UUID profileId
) {

}

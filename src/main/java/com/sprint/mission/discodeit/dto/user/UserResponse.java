package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String nickname,
    String username,
    String email,
    String phoneNumber,
    UUID profileId,
    UserStatusResponse status
) {

}

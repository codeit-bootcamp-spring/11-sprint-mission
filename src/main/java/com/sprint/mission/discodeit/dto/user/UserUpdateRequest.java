package com.sprint.mission.discodeit.dto.user;

import java.util.UUID;

public record UserUpdateRequest(
        UUID id,
        String nickname,
        String username,
        String email,
        String password,
        String phoneNumber
) {}

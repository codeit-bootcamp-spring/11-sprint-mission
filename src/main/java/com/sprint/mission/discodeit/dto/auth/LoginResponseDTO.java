package com.sprint.mission.discodeit.dto.auth;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.UUID;

public record LoginResponseDTO(
        UUID id,
        String username,
        String email,
        UUID profileId,
        String status
) {
    public static LoginResponseDTO from(User user, UserStatus userStatus) {
        return new LoginResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileId(),
                userStatus.calculateCurrentStatus()
        );
    }
}

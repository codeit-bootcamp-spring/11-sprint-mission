package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.UUID;

public record UpdateUserInfoResponseDTO(
        UUID id,
        String username,
        String email,
        UUID profileId,
        String status
) {
    public static UpdateUserInfoResponseDTO from(User user, UserStatus userStatus) {
        return new UpdateUserInfoResponseDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getProfileId(),
            userStatus.calculateCurrentStatus()
        );
    }
}

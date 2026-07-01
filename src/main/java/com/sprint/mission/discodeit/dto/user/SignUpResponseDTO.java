package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.UUID;

public record SignUpResponseDTO(
        UUID id,
        String username,
        String email,
        UUID profileId,
        String status
) {
    public static SignUpResponseDTO from(User user, UserStatus userStatus) {
        return new SignUpResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileId(),
                userStatus.calculateCurrentStatus()
        );
    }
}

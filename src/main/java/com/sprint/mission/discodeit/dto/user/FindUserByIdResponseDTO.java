package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.UUID;

public record FindUserByIdResponseDTO(
    UUID id,
    String username,
    String email,
    UUID profileId,
    String status
) {
    public static FindUserByIdResponseDTO from(User user, UserStatus userStatus) {
        return new FindUserByIdResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileId(),
                userStatus.calculateCurrentStatus()
        );
    }
}

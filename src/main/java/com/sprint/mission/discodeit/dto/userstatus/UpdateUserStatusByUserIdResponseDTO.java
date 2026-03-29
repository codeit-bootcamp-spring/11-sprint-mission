package com.sprint.mission.discodeit.dto.userstatus;

import com.sprint.mission.discodeit.entity.UserStatus;

public record UpdateUserStatusByUserIdResponseDTO(
        UserStatus userStatus
) {
    public static UpdateUserStatusByUserIdResponseDTO from(UserStatus userStatus) {
        return new UpdateUserStatusByUserIdResponseDTO(
                userStatus
        );
    }
}

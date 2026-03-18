package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userstatus.CreateUserStatusRequestDTO;
import com.sprint.mission.discodeit.dto.userstatus.UpdateUserStatusRequestDTO;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.UUID;

public interface UserStatusService {
    UserStatus create(
            CreateUserStatusRequestDTO dto
    );

    UserStatus find(
            UUID userStatusId
    );

    UserStatus update(
            UpdateUserStatusRequestDTO dto
    );

    UserStatus updateByUserId(
            UUID userId
    );

    void delete(
            UUID userStatusId
    );
}

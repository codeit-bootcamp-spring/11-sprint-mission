package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserStatusCreateDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateDto;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {
    UserStatus createUserStatus(UserStatusCreateDto statusCreateDto);
    UserStatus findUserStatus(UUID id);
    List<UserStatus> findAllUserStatus();
    void deleteUserStatus(UUID userId);
    UserStatus updateUserStatus(UserStatusUpdateDto request);
    UserStatus updateUserIdStatus(UUID userId);
}

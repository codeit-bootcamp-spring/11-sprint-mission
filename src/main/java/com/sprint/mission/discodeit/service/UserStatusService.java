package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.userStatus.CreateUserStatusRequest;
import com.sprint.mission.discodeit.dto.request.userStatus.UpdateUserStatusRequest;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {
    UserStatus createUserStatus(CreateUserStatusRequest request);
    UserStatus getUserStatusById(UUID id);
    List<UserStatus> getAllUserStatuses();
    void updateUserStatus(UUID id, UpdateUserStatusRequest request);
    void updateUserStatusByUserId(UUID userId);
    void deleteUserStatus(UUID id);
}
package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UserStatusService {
    UserStatus createUserStatus(UUID userId);
    UserStatus getUserStatusById(UUID id);
    UserStatus getUserStatusByUserId(UUID userId);
    List<UserStatus> getAllUserStatuses();
    void updateUserStatus(UUID id, Instant lastActiveAt);
    void deleteUserStatus(UUID id);
}

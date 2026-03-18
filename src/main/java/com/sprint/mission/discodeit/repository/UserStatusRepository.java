package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UserStatusRepository {
    UserStatus create(UserStatus userStatus);

    UserStatus readByUserId(UUID userId);

    List<UserStatus> readAll();

    UserStatus update(UUID userId, Instant lastOnlineAt);

    void delete(UUID userId);
}

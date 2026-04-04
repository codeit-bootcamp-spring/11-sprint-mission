package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {
    UserStatus create(UserStatusCreateRequest dto);

    UserStatus find(UUID id);

    List<UserStatus> findAll();

    UserStatus update(UUID id, UserStatusUpdateRequest dto);

    UserStatus updateByUserId(UUID userId);

    void delete(UUID id);
}

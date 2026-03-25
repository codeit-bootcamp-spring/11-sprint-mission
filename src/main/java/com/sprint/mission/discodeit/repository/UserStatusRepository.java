package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.UUID;

public interface UserStatusRepository {
    void insert(UserStatus userStatus);
    UserStatus findById(UUID id);
    UserStatus findByUserId(UUID userId);
    List<UserStatus> findAll();
    void update(UserStatus userStatus);
    void delete(UUID id);
    void deleteByUserId(UUID userId);
}

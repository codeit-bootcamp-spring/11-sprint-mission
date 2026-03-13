package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStatusRepository {
    public void init();
    public void save(UserStatus userStatus);
    public Optional<UserStatus> findById(UUID id);
    public List<UserStatus> findAll();
    public void delete(UserStatus userStatus);

    public Optional<UserStatus> findByUserId(UUID id);
    public void deleteByUserId(UUID id);
}

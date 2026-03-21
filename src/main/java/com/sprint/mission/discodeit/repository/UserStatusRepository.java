package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;

public interface UserStatusRepository {
    void save(UserStatus userStatus);
    UserStatus findById(Long id);
    List<UserStatus> findAll();
    void delete(Long id);
}
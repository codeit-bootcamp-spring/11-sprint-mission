package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserRepository {

    void insert(User user);

    User findById(UUID id); // 특정 유저 한명 find
    List<User> findAll(); // 모든 유저 조회

    void update(User user);

    void delete(UUID id);
}
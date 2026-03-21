package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserRepository {
    void save(User user);
    User findById(UUID id);
    User findByUsername(String username);
    boolean existByUsername(String username);
    boolean existByEmail(String email);
    List<User> findAll();
    void delete(User user);
}
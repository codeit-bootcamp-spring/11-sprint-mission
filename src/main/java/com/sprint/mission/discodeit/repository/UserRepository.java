package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Map<UUID, User> findAll();
    Optional<User> findById(UUID id);
    Optional<User> findByName(String name);
    void delete(UUID id);
}

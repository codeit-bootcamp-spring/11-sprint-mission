package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    public void init();
    public void save(User user);
    public Optional<User> findById(UUID id);
    public List<User> findAll();
    public void delete(User user);
}

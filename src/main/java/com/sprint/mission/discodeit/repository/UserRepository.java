package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

  void save(User user);

  Optional<User> findById(UUID id);

  Optional<User> findByUsername(String username);

  List<User> findAll();

  boolean existById(UUID id);

  boolean existByUsername(String username);

  boolean existByEmail(String email);

  void delete(User user);
}
package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserRepository {

  User create(User user);

  User read(UUID id);

  List<User> readAll();

  void delete(UUID id);

  User update(User user);

  void restore(UUID id);

  boolean existsByUserName(String userName);

  boolean existsByEmail(String userEmail);

  User findByUserName(String userName);

  boolean existsByUserNameExcluding(String userName, UUID excludeId);

  boolean existsByEmailExcluding(String userEmail, UUID excludeId);
}

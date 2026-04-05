package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

  boolean saveUser(User user);

  Optional<User> getUser(UUID userId);

  List<User> getAllUser();

  Optional<User> getUserByNickname(String nickname);

  boolean deleteUser(UUID userId);

  boolean isExistUserByNickname(String nickname);

  boolean isExistUserByEmail(String email);

  boolean isExistUser(UUID userId);


}

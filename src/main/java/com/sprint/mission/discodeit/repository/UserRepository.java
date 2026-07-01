package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.base.Repository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends Repository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
}

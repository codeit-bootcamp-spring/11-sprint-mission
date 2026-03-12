package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    void delete(User user);
}
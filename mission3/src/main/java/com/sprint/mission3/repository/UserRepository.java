package com.sprint.mission3.repository;

import com.sprint.mission3.domain.User;
import java.util.Optional

public class UserRepository {

    void save(User user);

    Optional<User> findById(Long id);
}

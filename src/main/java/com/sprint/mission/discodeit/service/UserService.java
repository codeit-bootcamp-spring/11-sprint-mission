package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User createUser(String name, String email, String password);
    User readUser(UUID id);
    List<User> allReadUser();
    void deleteUser(UUID id, String password);
    void updateUser(UUID id, String name, String email, String password);
}

package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User create(String name, String email, String password);
    User findById(UUID id);
    List<User> findAll();
    void update(User oldUser, User newUser);
    void delete(User user);
}

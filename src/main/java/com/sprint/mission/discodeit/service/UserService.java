package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;

import java.util.List;
import java.util.UUID;

public interface UserService {
    void createUser(User user);
    User findUser(UUID id);
    List<User> findAllUser();
    void updateUser(User oldUser, User newUser);
    void deleteUser(User user);
}

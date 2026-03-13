package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserCreateRequestDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User create(UserCreateRequestDto dto);
    User findById(UUID id);
    List<User> findAll();
    void update(UUID id, User newUser);
    void delete(User user);
}

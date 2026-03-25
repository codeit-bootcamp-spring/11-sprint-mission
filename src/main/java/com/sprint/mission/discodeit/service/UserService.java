package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserUpdateRequst;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserDto create(UserCreateRequest request);
    UserDto findById(UUID id);
    List<UserDto> findAll();
    void update(UUID id, UserUpdateRequst request);
    void delete(UUID id);
}
package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserLoginRequest;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDto create(UserCreateRequest request);
    UserDto findById(UUID id);
    List<UserDto> findAll();
    void update(UUID id, UserUpdateRequest request);
    void delete(UUID id);
    UserDto login(UserLoginRequest request);
}
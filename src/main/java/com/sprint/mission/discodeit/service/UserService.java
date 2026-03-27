package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDto create(UserCreateRequest dto);
    UserDto findById(UUID id);
    List<UserDto> findAll();
    void update(UUID id, UserUpdateRequest dto);
    void delete(UUID id);
}

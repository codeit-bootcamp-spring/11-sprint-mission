package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserDto create(UserCreateRequest request);
    UserDto updateRole(UserRoleUpdateRequest request);
    Optional<UserDto> find(UUID id);
    List<UserDto> findAll();
    UserDto update(UserUpdateParam param);
    void delete(UUID id);
}
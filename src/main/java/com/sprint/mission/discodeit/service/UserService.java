package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.UserCreateRequestDto;
import com.sprint.mission.discodeit.dto.user.UserResponseDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequestDto;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponseDto create(UserCreateRequestDto dto);
    UserResponseDto findById(UUID id);
    List<UserResponseDto> findAll();
    void update(UUID id, UserUpdateRequestDto dto);
    void delete(UUID id);
}

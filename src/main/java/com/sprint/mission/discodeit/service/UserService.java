package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserCreateRequestDto;
import com.sprint.mission.discodeit.dto.UserResponseDto;
import com.sprint.mission.discodeit.dto.UserUpdateRequestDto;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User create(UserCreateRequestDto dto);
    UserResponseDto findById(UUID id);
    List<UserResponseDto> findAll();
    void update(UserUpdateRequestDto dto);
    void delete(UUID id);
}

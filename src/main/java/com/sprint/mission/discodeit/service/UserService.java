package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserCreateRequestDto;
import com.sprint.mission.discodeit.dto.UserResponseDto;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User create(UserCreateRequestDto dto);
    UserResponseDto findById(UUID id);
    List<UserResponseDto> findAll();
    void update(UUID id, User newUser);
    void delete(User user);
}

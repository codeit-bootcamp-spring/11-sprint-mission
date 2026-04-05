package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.*;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponseDto create(UserCreateRequestDto dto);

    UserResponseDto findById(UUID id);

    List<UserResponseDto> findAll();

    UserResponseDto update(UUID id, UserUpdateRequestDto dto);

    UserStatusResponseDto updateOnlineStatus(UUID id, UserStatusResponseDto dto);

    boolean login(LoginRequestDto dto); // email, pw 받아서 성공/실패 boolean 반환

    void delete(UUID id);

    List<UserDto> getAllUsers();
}

package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {
    // 생성 읽기 모두읽기 수정 삭제 - 순서
    UserDto create(UserCreateRequest request);

    UserDto findById(UUID id);

    List<UserDto> findAll();

    UserDto update(UserUpdateRequest request);

    void delete(UUID id);

    UserDto updateStatus(UUID userId, UserStatusUpdateRequest request);
}

package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequestDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponseDto;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {
    UserStatusResponseDto create(UserStatusCreateRequestDto dto);
    UserStatusResponseDto find(UUID id);
    UserStatusResponseDto findByUserId(UUID id);
    List<UserStatusResponseDto> findAll();
    void update(UUID id);
    void delete(UUID id);

    void updateByUserId(UUID id);
}

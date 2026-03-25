package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateParam;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStatusService {
    UserStatusDto create(UserStatusCreateRequest request);
    Optional<UserStatusDto> find(UUID id);
    List<UserStatusDto> findAll();
    UserStatusDto update(UserStatusUpdateParam param);
    UserStatusDto updateByUserId(UUID userId, UserStatusUpdateRequest request);
    void delete(UUID id);
}
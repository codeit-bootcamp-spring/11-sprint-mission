package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {
    UserStatusDto create(UserStatusCreateRequest dto);
    UserStatusDto find(UUID id);
    UserStatusDto findByUserId(UUID id);
    List<UserStatusDto> findAll();
    void update(UUID id, UserStatusUpdateRequest dto);
    void delete(UUID id);

    void updateByUserId(UUID id, UserStatusUpdateRequest dto);
}

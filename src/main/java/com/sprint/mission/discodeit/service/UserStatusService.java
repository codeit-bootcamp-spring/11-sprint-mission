package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.UserStatusResponse;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {

    UserStatusResponse create(UserStatusCreateRequest request);

    UserStatusResponse findById(UUID id);

    List<UserStatusResponse> findAll();

    UserStatusResponse update(UUID id, UserStatusUpdateRequest request);

    UserStatusResponse updateByUserId(UUID id, UserStatusUpdateRequest request);

    void deleteById(UUID id);
}

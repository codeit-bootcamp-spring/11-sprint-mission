package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;

import java.util.UUID;

public interface UserStatusService {
    UserStatusDto updateUserStatus(UUID userId, UserStatusUpdateRequest request);
}
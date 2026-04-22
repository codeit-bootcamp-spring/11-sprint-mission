package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import java.util.UUID;

public interface UserStatusService {

  UserStatusDto update(UUID userId, UserStatusUpdateRequest request);
}

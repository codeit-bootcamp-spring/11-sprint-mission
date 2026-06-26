package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdatedRequest;

public interface AuthService {

    UserDto updateRole(UserRoleUpdatedRequest request);

}

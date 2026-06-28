package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.auth.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;

public interface AuthService {

  UserResponse updateRole(UserRoleUpdateRequest request);
}

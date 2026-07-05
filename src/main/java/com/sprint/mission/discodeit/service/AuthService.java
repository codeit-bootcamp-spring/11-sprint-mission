package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.auth.TokenRefreshResult;
import com.sprint.mission.discodeit.dto.auth.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;

public interface AuthService {

  TokenRefreshResult refresh(String refreshToken);

  UserResponse updateRole(UserRoleUpdateRequest request);

  UserResponse updateRoleInternal(UserRoleUpdateRequest request);
}

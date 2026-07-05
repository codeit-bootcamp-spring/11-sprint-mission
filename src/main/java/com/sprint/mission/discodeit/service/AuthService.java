package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import java.util.UUID;

public interface AuthService {

  UserDto updateRole(RoleUpdateRequest request);

  UserDto updateRoleInternal(RoleUpdateRequest request);

  void saveRefreshToken(UUID userId, String refreshToken);

  RefreshResult refresh(String refreshToken);
}

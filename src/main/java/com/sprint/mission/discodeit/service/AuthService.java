package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.security.jwt.dto.RefreshTokenResult;

public interface AuthService {

  UserDto updateRole(UserRoleUpdateRequest dto);

  RefreshTokenResult refresh(String refreshToken);

}

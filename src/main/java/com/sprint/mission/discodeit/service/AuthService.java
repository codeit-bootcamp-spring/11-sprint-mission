package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.security.UserRoleUpdateRequest;

public interface AuthService {

  UserDto updateRole(UserRoleUpdateRequest dto);
}

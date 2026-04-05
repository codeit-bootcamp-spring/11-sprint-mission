package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserLoginRequest;

public interface AuthService {

  UserDto login(UserLoginRequest request);
}

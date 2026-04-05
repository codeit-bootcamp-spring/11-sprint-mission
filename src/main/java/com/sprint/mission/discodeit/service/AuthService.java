package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.UserLoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;

public interface AuthService {
    UserResponse login(UserLoginRequest dto);
}

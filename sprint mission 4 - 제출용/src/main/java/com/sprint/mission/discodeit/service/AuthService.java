package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.auth.LoginDto;

public interface AuthService {

  LoginDto login(LoginRequest request);
}

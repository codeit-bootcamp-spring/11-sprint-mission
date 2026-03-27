package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.login.LoginRequest;
import com.sprint.mission.discodeit.dto.login.LoginResponseDto;

public interface AuthService {
    public LoginResponseDto login(LoginRequest dto);
}

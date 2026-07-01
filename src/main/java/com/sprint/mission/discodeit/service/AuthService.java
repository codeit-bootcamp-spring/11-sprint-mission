package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.auth.LoginRequestDTO;
import com.sprint.mission.discodeit.dto.auth.LoginResponseDTO;
import com.sprint.mission.discodeit.entity.User;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);
}

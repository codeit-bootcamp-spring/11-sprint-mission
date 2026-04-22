package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.authDto.LoginRequest;
import com.sprint.mission.discodeit.dto.userdto.UserDto;

public interface AuthService {

  UserDto login(LoginRequest loginRequest);


}

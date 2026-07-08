package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.security.jwt.dto.JwtDto;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

  UserDto updateRole(UserRoleUpdateRequest dto);

  JwtDto refresh(String refreshToken, HttpServletResponse response);

}

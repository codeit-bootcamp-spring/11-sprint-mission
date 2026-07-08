package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserRoleUpdateRequest;

public interface AuthService {

  UserDto.Response updateRole(UserRoleUpdateRequest request);
  
  void initAdmin(UserDto.CreateRequest request);
}
package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userstatusdto.CreateUserStatusDto;
import com.sprint.mission.discodeit.dto.userstatusdto.UpdateUserStatusDto;
import com.sprint.mission.discodeit.dto.userstatusdto.UserStatusInfoDto;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {

  UserStatusInfoDto create(CreateUserStatusDto createUserStatusDto);

  UserStatusInfoDto find(UUID userId);

  List<UserStatusInfoDto> findAll();

  UserStatusInfoDto update(UUID userId, UpdateUserStatusDto updateUserStatusDto);

  boolean delete(UUID userId);


}

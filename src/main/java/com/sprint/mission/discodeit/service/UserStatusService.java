package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userstatusdto.CreateUserStatusDto;
import com.sprint.mission.discodeit.dto.userstatusdto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatusdto.UserStatusDto;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {

  UserStatusDto create(CreateUserStatusDto createUserStatusDto);

  UserStatusDto find(UUID userId);

  List<UserStatusDto> findAll();

  UserStatusDto update(UUID userId, UserStatusUpdateRequest userStatusUpdateRequest);

  void delete(UUID userId);


}

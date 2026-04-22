package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateDto;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {

  UserStatusDto createUserStatus(UserStatusCreateRequest statusCreateDto);

  UserStatusDto findUserStatus(UUID id);

  List<UserStatusDto> findAllUserStatus();

  void deleteUserStatus(UUID userId);

  UserStatusDto updateUserIdStatus(UUID userId, UserStatusUpdateDto request);
}

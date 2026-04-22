package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import java.util.List;
import java.util.UUID;

public interface UserStatusService {

  UserStatusDto create(UserStatusCreateRequest dto);

  UserStatusDto findByUserId(UUID userId);

  List<UserStatusDto> findAll();

  UserStatusDto update(UUID userId, UserStatusUpdateRequest dto);

  //void updateByUserId(UUID userId);

  void delete(UUID id);
}

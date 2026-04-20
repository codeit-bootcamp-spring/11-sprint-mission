package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.util.List;
import java.util.UUID;

public interface UserStatusService {

  UserStatus create(UserStatusCreateRequest dto);

  UserStatusDto find(UUID id);

  List<UserStatusDto> findAll();

  UserStatus update(UUID id, UserStatusUpdateRequest dto);

  UserStatus updateByUserId(UUID userId, UserStatusUpdateRequest dto);

  void delete(UUID id);
}

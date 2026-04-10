package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import java.util.List;
import java.util.UUID;

public interface UserStatusService {

  UserStatusResponse createUserStatus(UserStatusCreateRequest userStatusCreateRequest);

  UserStatusResponse findById(UUID id);

  List<UserStatusResponse> findAll();

  UserStatusResponse updateUserStatusByUserId(UUID userId);

  void deleteUserStatus(UUID id);
}

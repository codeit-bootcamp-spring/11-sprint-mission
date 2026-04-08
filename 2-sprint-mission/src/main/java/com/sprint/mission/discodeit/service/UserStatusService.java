package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserStatusDto;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {

  UserStatusDto.Response create(UserStatusDto.CreateRequest request);

  UserStatusDto.Response findById(UUID id);

  List<UserStatusDto.Response> findAll();

  UserStatusDto.Response update(UUID id, UserStatusDto.UpdateRequest request);

  UserStatusDto.Response updateByUserId(UUID userId,
      UserStatusDto.UpdateRequest request); // 유저 ID로 업데이트

  void delete(UUID id);
}
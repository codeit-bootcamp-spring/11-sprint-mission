package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {

  ReadStatus create(ReadStatusCreateRequest request);

  ReadStatus find(UUID id);

  // User의 모든 채널 ReadStatus 조회
  List<ReadStatus> findAllByUserId(UUID userId);

  void update(ReadStatusUpdateRequest request);

  void delete(UUID id);
}

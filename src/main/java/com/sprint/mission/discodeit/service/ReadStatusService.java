package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import java.util.List;
import java.util.UUID;

public interface ReadStatusService {

  ReadStatusDto create(ReadStatusCreateRequest dto);

  ReadStatusDto find(UUID id);

  List<ReadStatusDto> findAllByUserId(List<UUID> userIds);

  ReadStatusDto update(UUID id, ReadStatusUpdateRequest dto);

  void delete(UUID id);
}

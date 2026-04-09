package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {

  ReadStatusDto createReadStatus(ReadStatusCreateRequest statusCreateDto);

  ReadStatusDto findById(UUID id);

  List<ReadStatusDto> findAllByUserId(UUID authorId);

  void deleteReadStatus(UUID id);

  ReadStatusDto updateStatus(UUID readStatusId, ReadStatusUpdateRequest request);
}

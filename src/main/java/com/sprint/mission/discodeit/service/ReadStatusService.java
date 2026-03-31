package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.ReadStatusCreateDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateDto;
import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {

  void createReadStatus(ReadStatusCreateDto statusCreateDto);

  ReadStatus findById(UUID id);

  List<ReadStatus> findAllByUserId(UUID authorId);

  void deleteReadStatus(UUID id);

  ReadStatus updateStatus(UUID readStatusId, ReadStatusUpdateDto request);
}

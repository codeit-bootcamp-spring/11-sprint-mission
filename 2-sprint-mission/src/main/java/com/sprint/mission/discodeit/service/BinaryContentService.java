package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import java.util.List;
import java.util.UUID;

public interface BinaryContentService {

  BinaryContentDto.Response create(BinaryContentDto.CreateRequest request);

  BinaryContentDto.Response findById(UUID id);

  List<BinaryContentDto.Response> findAllByIdIn(List<UUID> ids);

  void updateStatus(UUID id, BinaryContentStatus status);

  void delete(UUID id);
}
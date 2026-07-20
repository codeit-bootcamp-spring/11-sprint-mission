package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent.BinaryContentStatus;
import java.util.List;
import java.util.UUID;

public interface BinaryContentService {

  BinaryContentDto create(BinaryContentCreateRequest dto);

  BinaryContentDto find(UUID id);

  List<BinaryContentDto> findAllByIdIn(List<UUID> ids);

  BinaryContentDto updateStatus(UUID id, BinaryContentStatus status);

  void delete(UUID id);
}

package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.BinaryContentCreateDto;
import com.sprint.mission.discodeit.dto.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface BinaryContentService {

  BinaryContent createBinaryContent(BinaryContentCreateDto request);

  BinaryContent find(UUID id);

  List<BinaryContentResponse> findAllByIdIn(Collection<UUID> ids);

  void deleteBinaryContent(UUID id);
}

package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentDto;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;

public interface BinaryContentService {

  //Create
  BinaryContentDto create(BinaryContentCreateRequest request, UUID ownerId);

  //Read
  BinaryContentDto findById(UUID binaryContentId);

  //Read all
  List<BinaryContentDto> findAllByIdIn(List<UUID> ids);

  //Delete
  void delete(UUID binaryContentId);

  //Download
  Resource download(UUID binaryContentId);

  BinaryContentDto updateStatus(UUID binaryContentId, BinaryContentStatus status);
}

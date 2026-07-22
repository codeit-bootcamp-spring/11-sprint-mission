package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.binarycontentdto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent.BinaryContentStatus;
import java.util.List;
import java.util.UUID;

public interface BinaryContentService {

  BinaryContentDto create(BinaryContentCreateRequest binaryContentCreateRequest);

  BinaryContentDto find(UUID binaryContentId);

  List<BinaryContentDto> findAll();

  void updateStatus(UUID binaryContentId, BinaryContentStatus status);


  boolean delete(UUID binaryContentId);


}

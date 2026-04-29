package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface BinaryContentService {

  BinaryContentDto createBinaryContent(BinaryContentCreateRequest request);

  BinaryContentDto find(UUID id);

  List<BinaryContentDto> findAllByIdIn(Collection<UUID> ids);

  void deleteBinaryContent(UUID id);

  public ResponseEntity<Resource> download(UUID id);
}

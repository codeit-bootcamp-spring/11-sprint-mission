package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.BusinessLogicException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;

  @Override
  @Transactional
  public BinaryContent createBinaryContent(BinaryContentCreateDto request) {
    if (request.data() == null || request.data().length == 0) {
      throw new BusinessLogicException(ExceptionCode.FILE_EMPTY);
    }

    BinaryContent content = new BinaryContent(request.data(), request.fileName(), request.contentType());

    return binaryContentRepository.save(content);
  }

  @Override
  public BinaryContent find(UUID id) {
    return binaryContentRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.FILE_NOT_FOUND));
  }

  @Override
  public List<BinaryContent> findAllByIdIn(Collection<UUID> ids) {
    if (ids == null) {
      return Collections.emptyList();
    }

    return binaryContentRepository.findAllById(ids);
  }

  @Override
  @Transactional
  public void deleteBinaryContent(UUID id) {
    BinaryContent binaryContent = binaryContentRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.BINARY_CONTENT_NOT_EXISTS));
    binaryContentRepository.delete(binaryContent);
  }
}

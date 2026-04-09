package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateDto;
import com.sprint.mission.discodeit.dto.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.BusinessLogicException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;

  @Override
  public BinaryContent createBinaryContent(BinaryContentCreateDto request) {
    if (request.data() == null || request.data().length == 0) {
      throw new BusinessLogicException(ExceptionCode.FILE_EMPTY);
    }

    BinaryContent content = BinaryContent.builder()
        .id(UUID.randomUUID())
        .data(request.data())
        .fileName(request.fileName())
        .contentType(request.contentType())
        .size(request.data().length)
        .createdAt(Instant.now())
        .build();

    return binaryContentRepository.save(content);
  }

  @Override
  public BinaryContent find(UUID id) {
    return binaryContentRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.FILE_NOT_FOUND));
  }

  @Override
  public List<BinaryContentResponse> findAllByIdIn(Collection<UUID> ids) {
    if (ids == null || ids.isEmpty()) {
      return Collections.emptyList();
    }

    return binaryContentRepository.findAllById(ids)
        .stream()
        .map(BinaryContentResponse::from)
        .toList();
  }

  @Override
  public void deleteBinaryContent(UUID id) {
    binaryContentRepository.findById(id)
        .ifPresent(content -> {
          binaryContentRepository.delete(id);
          System.out.println("✅ 바이너리 콘텐츠 삭제 완료");
        });
  }
}

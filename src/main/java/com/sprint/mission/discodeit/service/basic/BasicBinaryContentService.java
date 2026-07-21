package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binarycontent.MissingFileContentException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public BinaryContentDto create(BinaryContentCreateRequest dto) {
    log.debug("create 시작 - 입력값: {}", dto);

    if (dto.bytes() == null) {
      throw new MissingFileContentException();
    }
    String fileName = dto.fileName();
    byte[] bytes = dto.bytes();
    String contentType = dto.contentType();
    BinaryContent binaryContent = new BinaryContent(
        fileName,
        (long) bytes.length,
        contentType
    );
    binaryContent = binaryContentRepository.save(binaryContent);

    eventPublisher.publishEvent(new BinaryContentCreatedEvent(binaryContent.getId(), bytes));

    log.info("파일 메타데이터 DB 저장 완료 및 이벤트 발행 - id: {}", binaryContent.getId());
    return binaryContentMapper.toDto(binaryContent);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public void updateStatus(UUID id, BinaryContentStatus status) {
    BinaryContent binaryContent = binaryContentRepository.findById(id)
        .orElseThrow(() -> new BinaryContentNotFoundException(id));

    binaryContent.updateStatus(status);
    log.debug("Binary Content 상태 업데이트 완료 - id: {}, status: {}", id, status);
  }

  @Override
  public BinaryContentDto find(UUID id) {
    BinaryContent binaryContent = binaryContentRepository.findById(id)
        .orElseThrow(
            () -> new BinaryContentNotFoundException(id));

    return binaryContentMapper.toDto(binaryContent);
  }

  @Override
  public List<BinaryContentDto> findAllByIdIn(List<UUID> uuids) {
    if (uuids == null || uuids.isEmpty()) {
      return List.of();
    }
    return binaryContentRepository.findAllById(uuids).stream()
        .map(binaryContentMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    log.debug("delete 시작 - 입력값: {}", id);

    BinaryContent binaryContent = binaryContentRepository.findById(id)
        .orElseThrow(
            () -> new BinaryContentNotFoundException(id));

    binaryContentRepository.delete(binaryContent);
    log.info("파일 삭제 완료 - binaryContentId: {}", binaryContent.getId());
  }
}

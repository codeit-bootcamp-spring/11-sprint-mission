package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.binaryContent.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.binaryContent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binaryContent.FileSizeExceededException;
import com.sprint.mission.discodeit.exception.binaryContent.InvalidContentTypeException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper;
  private final BinaryContentStorage binaryContentStorage;
  private final ApplicationEventPublisher eventPublisher;

  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
  private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
      "image/jpeg", "image/png", "image/gif", "image/webp"
  );

  //create
  @Transactional
  @Override
  public BinaryContentDto create(BinaryContentCreateRequest request, UUID ownerId) {
    log.debug("첨부파일 업로드 시작");
    // 용량 제한
    if (request.bytes().length > MAX_FILE_SIZE) {
      log.warn("파일 크기 초과 최대 10MB - size: {}", request.bytes().length);
      throw new FileSizeExceededException(request.bytes().length);
    }
    // 확장자 제한
    if (!ALLOWED_CONTENT_TYPES.contains(request.contentType())) {
      log.warn("허용되지 않는 확장자 - contentType: {}", request.contentType());
      throw new InvalidContentTypeException(request.contentType());
    }

    BinaryContent binaryContent = new BinaryContent(
        request.contentType(),
        request.bytes()
    );

    binaryContentRepository.save(binaryContent);
    eventPublisher.publishEvent(
        new BinaryContentCreatedEvent(binaryContent.getId(), request.bytes(), ownerId)
    );

    log.info("첨부파일 업로드 완료 - binaryContentId: {}", binaryContent.getId());
    return binaryContentMapper.toDto(binaryContent);
  }

  //Read
  @Override
  @Transactional(readOnly = true)
  public BinaryContentDto findById(UUID binaryContentId) {
    return binaryContentMapper.toDto(findBinaryContentOrThrow(binaryContentId));
  }

  //Read all
  @Override
  @Transactional(readOnly = true)
  public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
    return binaryContentRepository.findAllById(ids)
        .stream()
        .map(binaryContentMapper::toDto)
        .toList();
  }

  //Delete
  @Transactional
  @Override
  public void delete(UUID binaryContentId) {
    log.debug("파일 삭제 시작 - binaryContentId: {}", binaryContentId);
    findBinaryContentOrThrow(binaryContentId);
    binaryContentStorage.delete(binaryContentId);
    binaryContentRepository.deleteById(binaryContentId);
    log.info("파일 삭제 완료 - binaryContentId: {}", binaryContentId);
  }

  //Download
  @Override
  @Transactional(readOnly = true)
  public Resource download(UUID binaryContentId) {
    log.debug("파일 다운로드 시작 - binaryContentId: {}", binaryContentId);
    BinaryContentDto dto = findById(binaryContentId);
    Resource resource = binaryContentStorage.download(dto.id());
    log.info("파일 다운로드 완료 - binaryContentId: {}", binaryContentId);
    return resource;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public BinaryContentDto updateStatus(UUID binaryContentId, BinaryContentStatus status) {
    BinaryContent binaryContent = findBinaryContentOrThrow(binaryContentId);
    binaryContent.updateStatus(status);
    log.info("BinaryContent 상태 업데이트 - id: {}, status: {}", binaryContentId, status);
    return binaryContentMapper.toDto(binaryContent);
  }

  private BinaryContent findBinaryContentOrThrow(UUID binaryContentId) {
    return binaryContentRepository.findById(binaryContentId)
        .orElseThrow(() -> {
          log.warn("컨텐츠를 찾을 수 없음 - binaryContentId: {}", binaryContentId);
          return new BinaryContentNotFoundException(binaryContentId);
        });
  }
}

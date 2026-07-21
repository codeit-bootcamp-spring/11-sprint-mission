package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.SseService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper;
  private final BinaryContentStorage binaryContentStorage;
  private final ApplicationEventPublisher eventPublisher;
  private final SseService sseService;

  @Override
  @Transactional
  public BinaryContentDto create(BinaryContentCreateRequest request) {
    log.debug("파일 업로드 시작 - fileName: {}, size: {}", request.fileName(), request.size());

    BinaryContent binaryContent = new BinaryContent(request.fileName(), request.size(),
        request.contentType());

    binaryContentRepository.save(binaryContent);

    eventPublisher.publishEvent(
        new BinaryContentCreatedEvent(binaryContent.getId(), request.bytes()));

    log.info("파일 업로드 완료 - fileId: {}", binaryContent.getId());

    return binaryContentMapper.toDto(binaryContent);
  }

  @Override
  public BinaryContentDto findById(UUID id) {
    log.debug("파일 조회 시작 - fileId: {}", id);
    BinaryContent binaryContent = binaryContentRepository.findById(id).orElseThrow(() -> {
      log.warn("파일 조회 실패(존재하지 않는 파일) - fileId: {}", id);
      return new BinaryContentNotFoundException(id);
    });
    log.info("파일 조회 완료 - fileId: {}", id);
    return binaryContentMapper.toDto(binaryContent);
  }

  @Override
  public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
    return binaryContentRepository.findAllByIdIn(ids).stream().map(binaryContentMapper::toDto)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    log.debug("파일 삭제 시작 - fileId: {}", id);
    binaryContentRepository.deleteById(id);
    binaryContentStorage.delete(id);
    log.info("파일 삭제 완료 - fileId: {}", id);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public BinaryContentDto updateStatus(UUID binaryContentId, BinaryContentStatus status) {
    log.debug("파일 상태 업데이트 시작 - fileId: {}, status: {}", binaryContentId, status);
    BinaryContent binaryContent = binaryContentRepository.findById(binaryContentId)
        .orElseThrow(() -> new BinaryContentNotFoundException(binaryContentId));

    binaryContent.updateStatus(status);
    BinaryContentDto dto = binaryContentMapper.toDto(binaryContent);
    sseService.broadcast("binarycontents.updated", dto);
    
    log.info("파일 상태 업데이트 완료 - fileId: {}, status: {}", binaryContentId, status);
    return dto;
  }
}

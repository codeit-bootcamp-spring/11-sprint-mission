package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper mapper;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  @Override
  public BinaryContentResponse createBinaryContent(BinaryContentCreateRequest req) {
    log.debug("binary-content create trial: file-name={}, content-type={}, size={}", req.fileName(),
        req.contentType(), req.size());
    BinaryContent binaryContent = new BinaryContent(req.fileName(), req.size(), req.contentType());
    this.binaryContentRepository.save(binaryContent);

    this.eventPublisher.publishEvent(
        new BinaryContentCreatedEvent(binaryContent.getId(), req.bytes()));

    log.info("binary-content create success: id={}, file-name={}, content-type={}, size={}",
        binaryContent.getId(), binaryContent.getFileName(), binaryContent.getContentType(),
        binaryContent.getSize());
    return this.mapper.toResponse(binaryContent);
  }

  @Override
  public BinaryContentResponse findById(UUID id) {
    log.debug("binary-content find-by-id trial: id={}", id);
    BinaryContent binaryContent = this.binaryContentRepository.findById(id)
        .orElseThrow(() -> BinaryContentNotFoundException.withId(id));

    log.info("binary-content find-by-id success: id={}, file-name={}, content-type={}, size={}",
        binaryContent.getId(), binaryContent.getFileName(), binaryContent.getContentType(),
        binaryContent.getSize());
    return this.mapper.toResponse(binaryContent);
  }

  @Override
  public List<BinaryContentResponse> findAllByIdIn(List<UUID> ids) {
    log.debug("binary-content find-all-by-id-in trial: ids={}", ids);
    List<BinaryContent> binaryContents = this.binaryContentRepository.findAllByIdIn(ids);

    log.info("binary-content find-all-by-id-in success: count={}", binaryContents.size());
    return binaryContents.stream()
        .map(this.mapper::toResponse)
        .toList();
  }

  @Transactional
  @Override
  public void deleteBinaryContent(UUID id) {
    log.debug("binary-content delete trial: id={}", id);
    BinaryContent binaryContent = this.binaryContentRepository.findById(id)
        .orElseThrow(() -> BinaryContentNotFoundException.withId(id));

    this.binaryContentRepository.delete(binaryContent);

    log.info("binary-content delete success: id={}", binaryContent.getId());
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public void updateStatus(UUID id, BinaryContentStatus status) {
    log.debug("binary-content update-status trial: id={}, status={}", id, status);
    BinaryContent binaryContent = this.binaryContentRepository.findById(id)
        .orElseThrow(() -> BinaryContentNotFoundException.withId(id));

    binaryContent.updateStatus(status);

    log.info("binary-content update-status success: id={}, status={}", id, status);
  }
}
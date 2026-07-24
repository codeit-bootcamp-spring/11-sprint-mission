package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.BinaryContentDto.Response;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
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
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public BinaryContentDto.Response create(BinaryContentDto.CreateRequest request) {
    log.debug("바이너리 컨텐츠 생성 시작: fileName={}, contentType={}, size={} bytes",
        request.fileName(), request.contentType(), request.size());

    BinaryContent binaryContent = request.toEntity();
    binaryContentRepository.save(binaryContent);

    eventPublisher.publishEvent(
        new BinaryContentCreatedEvent(binaryContent.getId(), request.bytes()));

    log.info("바이너리 컨텐츠 생성 완료: binaryContentId={}", binaryContent.getId());
    return binaryContentMapper.toDto(binaryContent);
  }

  @Override
  public Response findById(UUID id) {
    log.debug("바이너리 컨텐츠 단건 조회 시작: id={}", id);

    Response response = binaryContentRepository.findById(id)
        .map(binaryContentMapper::toDto)
        .orElseThrow(() -> BinaryContentNotFoundException.withId(id));

    log.info("바이너리 컨텐츠 단건 조회 완료: id={}", id);
    return response;
  }

  @Override
  public List<Response> findAllByIdIn(List<UUID> ids) {
    log.debug("바이너리 컨텐츠 다건 조회 시작: 요청 건수={}", ids != null ? ids.size() : 0);

    List<Response> responses = binaryContentRepository.findAllByIdIn(ids).stream()
        .map(binaryContentMapper::toDto)
        .toList();

    log.info("바이너리 컨텐츠 다건 조회 완료: 총 {}건", responses.size());
    return responses;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateStatus(UUID id, BinaryContentStatus status) {
    BinaryContent binaryContent = binaryContentRepository.findById(id)
        .orElseThrow(() -> BinaryContentNotFoundException.withId(id));

    binaryContent.updateStatus(status);
    log.debug("바이너리 컨텐츠 상태 변경: binaryContentId={}, status={}", id, status);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    log.debug("바이너리 컨텐츠 삭제 시작: id={}", id);

    if (!binaryContentRepository.existsById(id)) {
      throw BinaryContentNotFoundException.withId(id);
    }

    binaryContentRepository.deleteById(id);
    log.info("바이너리 컨텐츠 삭제 완료: id={}", id);
  }
}
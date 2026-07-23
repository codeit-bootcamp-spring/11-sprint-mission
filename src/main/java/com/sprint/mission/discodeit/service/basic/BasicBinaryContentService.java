package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;

  @Override
  public BinaryContentDto findById(UUID id) {
    log.debug("파일 단건 조회 - id: {}", id);
    BinaryContent bc = binaryContentRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("파일 조회 실패 - 존재하지 않는 id: {}", id);
          return new IllegalArgumentException("BinaryContent with id " + id + " not found");
        });
    log.debug("파일 조회 완료 - id: {}, fileName: {}", id, bc.getFileName());
    return toDto(bc);
  }

  @Override
  public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
    log.debug("파일 다건 조회 - ids: {}", ids);
    return binaryContentRepository.findAllByIdIn(ids).stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    log.debug("파일 삭제 요청 - id: {}", id);
    binaryContentRepository.deleteById(id);
    log.info("파일 삭제 완료 - id: {}", id);
  }

  // 업로드 이벤트 리스너(BinaryContentUploadEventListener)의 원본 트랜잭션은
  // 이미 커밋된 상태이므로, 상태 업데이트는 별도 트랜잭션(REQUIRES_NEW)으로 분리한다.
  // 별도 클래스(서비스)의 메서드이므로 Spring AOP 프록시를 정상적으로 거쳐
  // @Transactional이 적용된다 (self-invocation 문제 없음).
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateStatus(UUID id, BinaryContentStatus status) {
    binaryContentRepository.findById(id)
        .ifPresent(binaryContent -> {
          binaryContent.updateStatus(status);
          binaryContentRepository.save(binaryContent);
        });
  }

  private BinaryContentDto toDto(BinaryContent bc) {
    return new BinaryContentDto(
        bc.getId(),
        bc.getFileName(),
        bc.getSize(),
        bc.getContentType()
    );
  }
}
package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinaryContentUploadEventListener {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentRepository binaryContentRepository;

  // 메타데이터 저장 트랜잭션이 커밋된 이후에만 실행됨
  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(BinaryContentCreatedEvent event) {
    try {
      binaryContentStorage.put(event.binaryContentId(), event.bytes());
      updateStatus(event.binaryContentId(), BinaryContentStatus.SUCCESS);
      log.debug("바이너리 데이터 저장 완료 - id: {}", event.binaryContentId());
    } catch (Exception e) {
      log.error("바이너리 데이터 저장 실패 - id: {}", event.binaryContentId(), e);
      updateStatus(event.binaryContentId(), BinaryContentStatus.FAIL);
    }
  }

  // 원본 메타데이터 저장 트랜잭션은 이미 커밋된 상태이므로, 상태 업데이트는 별도 트랜잭션으로 분리
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateStatus(UUID binaryContentId, BinaryContentStatus status) {
    binaryContentRepository.findById(binaryContentId)
        .ifPresent(binaryContent -> binaryContent.updateStatus(status));
  }
}

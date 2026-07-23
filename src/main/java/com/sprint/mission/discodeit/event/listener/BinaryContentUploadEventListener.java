package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.sse.BinaryContentStatusDto;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinaryContentUploadEventListener {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentService binaryContentService;
  private final ApplicationEventPublisher eventPublisher;

  // 메타데이터 저장 트랜잭션이 커밋된 이후에만 실행됨
  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(BinaryContentCreatedEvent event) {
    BinaryContentStatus resultStatus;
    try {
      binaryContentStorage.put(event.binaryContentId(), event.bytes());
      resultStatus = BinaryContentStatus.SUCCESS;
      log.debug("바이너리 데이터 저장 완료 - id: {}", event.binaryContentId());
    } catch (Exception e) {
      log.error("바이너리 데이터 저장 실패 - id: {}", event.binaryContentId(), e);
      resultStatus = BinaryContentStatus.FAIL;
    }
    binaryContentService.updateStatus(event.binaryContentId(), resultStatus);

    eventPublisher.publishEvent(new BinaryContentUpdatedEvent(
        new BinaryContentStatusDto(event.binaryContentId(), resultStatus),
        Instant.now(),
        event.uploaderId()
    ));
  }
}
package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.sse.SseEvents.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@AllArgsConstructor
public class BinaryContentEventListener {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentService binaryContentService;
  private final ApplicationEventPublisher eventPublisher;

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleBinaryContentCreatedEvent(BinaryContentCreatedEvent event) {
    log.info("바이너리 파일 스토리지 업로드 시작 - fileId: {}", event.getBinaryContentId());

    try {
      binaryContentStorage.put(event.getBinaryContentId(), event.getBytes());

      binaryContentService.updateStatus(event.getBinaryContentId(), BinaryContentStatus.SUCCESS);
      log.info("바이너리 파일 스토리지 업로드 완료 상태 업데이트 - fileId: {}", event.getBinaryContentId());

      eventPublisher.publishEvent(
          new BinaryContentUpdatedEvent(event.getBinaryContentId(), BinaryContentStatus.SUCCESS)
      );
    } catch (Exception e) {
      log.error("바이너리 파일 스토리지 업로드 중 오류 발생 - fileId: {}", event.getBinaryContentId(), e);

      binaryContentService.updateStatus(event.getBinaryContentId(), BinaryContentStatus.FAIL);

      eventPublisher.publishEvent(
          new BinaryContentUpdatedEvent(event.getBinaryContentId(), BinaryContentStatus.FAIL)
      );
    }
  }
}
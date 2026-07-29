package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.dto.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.dto.BinaryContentUpdatedEvent;
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
public class BinaryContentEventListener {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentService binaryContentService;
  private final ApplicationEventPublisher eventPublisher;

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(BinaryContentCreatedEvent event) {
    log.info("이벤트 수신: S3 업로드 시작 - id: {}", event.id());

    try {
      binaryContentStorage.put(event.id(), event.bytes());

      binaryContentService.updateStatus(event.id(), BinaryContentStatus.SUCCESS);

      BinaryContentDto binaryContentDto = binaryContentService.findById(event.id());

      eventPublisher.publishEvent(
          new BinaryContentUpdatedEvent(binaryContentDto, event.uploaderId(), Instant.now()));

      log.info("S3 업로드 및 상태 업데이트 완료 - id: {}", event.id());
    } catch (Exception e) {
      log.error("바이너리 데이터 저장 실패 - id: {}", event.id());
      binaryContentService.updateStatus(event.id(), BinaryContentStatus.FAIL);
    }
  }
}
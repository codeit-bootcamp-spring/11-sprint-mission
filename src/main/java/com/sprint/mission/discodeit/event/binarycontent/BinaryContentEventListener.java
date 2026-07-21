package com.sprint.mission.discodeit.event.binarycontent;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(BinaryContentCreatedEvent event) {
    log.info("이벤트 수신: S3 업로드 시작 - id: {}", event.id());

    try {
      binaryContentStorage.put(event.id(), event.bytes());

      binaryContentService.updateStatus(event.id(), BinaryContentStatus.SUCCESS);

      log.info("S3 업로드 및 상태 업데이트 완료 - id: {}", event.id());
    } catch (Exception e) {
      log.error("바이너리 데이터 저장 실패 - id: {}", event.id());
      binaryContentService.updateStatus(event.id(), BinaryContentStatus.FAIL);
    }
  }
}
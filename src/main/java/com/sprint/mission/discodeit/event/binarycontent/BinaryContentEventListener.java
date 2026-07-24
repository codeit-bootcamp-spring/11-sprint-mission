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
@RequiredArgsConstructor
@Component
public class BinaryContentEventListener {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentService binaryContentService;

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(BinaryContentCreatedEvent event) {
    log.debug("binary-content upload trial: id={}", event.binaryContentId());

    try {
      this.binaryContentStorage.put(event.binaryContentId(), event.bytes());
      this.binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.SUCCESS);

      log.info("binary-content upload success: id={}", event.binaryContentId());
    } catch (Exception e) {
      log.error("binary-content upload fail: id={}", event.binaryContentId(), e);

      this.binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.FAIL);
    }
  }
}
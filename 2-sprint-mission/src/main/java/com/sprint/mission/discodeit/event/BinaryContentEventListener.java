package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinaryContentEventListener {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentService binaryContentService;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(BinaryContentCreatedEvent event) {
    UUID binaryContentId = event.binaryContentId();
    log.debug("바이너리 데이터 저장 시작: binaryContentId={}", binaryContentId);

    try {
      binaryContentStorage.put(binaryContentId, event.bytes());
      binaryContentService.updateStatus(binaryContentId, BinaryContentStatus.SUCCESS);

      log.info("바이너리 데이터 저장 완료: binaryContentId={}", binaryContentId);
    } catch (Exception e) {
      log.error("바이너리 데이터 저장 실패: binaryContentId={}", binaryContentId, e);
      binaryContentService.updateStatus(binaryContentId, BinaryContentStatus.FAIL);
    }
  }
}
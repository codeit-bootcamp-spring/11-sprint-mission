package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.BinaryContentUploadStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class BinaryContentCreatedEventListener {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentService binaryContentService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(BinaryContentCreatedEvent event) {
    UUID binaryContentId = event.binaryContentId();
    log.debug("바이너리 데이터 저장 시작: id={}, size={}", binaryContentId, event.bytes().length);
    try {
      binaryContentStorage.put(binaryContentId, event.bytes());
      binaryContentService.updateStatus(binaryContentId, BinaryContentUploadStatus.SUCCESS);
      log.info("바이너리 데이터 저장 완료: id={}", binaryContentId);
    } catch (Exception e) {
      binaryContentService.updateStatus(binaryContentId, BinaryContentUploadStatus.FAIL);
      log.error("바이너리 데이터 저장 실패: id={}", binaryContentId, e);
    }
  }
}
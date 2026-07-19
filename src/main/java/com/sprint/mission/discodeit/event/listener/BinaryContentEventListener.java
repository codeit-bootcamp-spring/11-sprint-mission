package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * BinaryContent 메타 데이터 저장 트랜잭션이 커밋된 이후, 실제 바이너리 데이터를 스토리지에 저장하는
 * 리스너입니다. 메타 데이터 저장 트랜잭션이 바이너리 데이터 저장(오래 걸릴 수 있는 연산)을 기다리지
 * 않도록 트랜잭션에서 분리되어 있습니다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BinaryContentEventListener {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentService binaryContentService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleBinaryContentCreated(BinaryContentCreatedEvent event) {
    log.debug("바이너리 데이터 저장 시작: binaryContentId={}", event.binaryContentId());
    try {
      binaryContentStorage.put(event.binaryContentId(), event.bytes());
      binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.SUCCESS);
      log.info("바이너리 데이터 저장 성공: binaryContentId={}", event.binaryContentId());
    } catch (Exception e) {
      log.error("바이너리 데이터 저장 실패: binaryContentId={}, error={}",
          event.binaryContentId(), e.getMessage(), e);
      binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.FAIL);
    }
  }
}

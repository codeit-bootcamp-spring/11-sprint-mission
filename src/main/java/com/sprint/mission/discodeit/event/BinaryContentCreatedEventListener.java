package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.BinaryContent.BinaryContentStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BinaryContentCreatedEventListener {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentService binaryContentService;

  // 비동기 처리(eventTaskExecutor라는 이름을 가진 Executor의 스레드 풀에서 비동기로 실행)
  @Async("eventTaskExecutor")
  // 커밋 이후에 put 메서드가 실행되도록 설정
  // phase 생략 시 기본값 TransactionPhase.AFTER_COMMIT
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(BinaryContentCreatedEvent event) {

    try {
      binaryContentStorage.put(event.binaryContentId(), event.bytes());

      // 성공 시 BinaryContent Status를 성공 상태로 변경
      binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.SUCCESS);
    } catch (Exception e) {
      // 실패 시 BinaryContent Status를 실패 상태로 변경
      binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.FAIL);
    }
  }

}

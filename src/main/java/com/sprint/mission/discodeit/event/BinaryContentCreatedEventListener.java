package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinaryContentCreatedEventListener {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;

  @Transactional
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(BinaryContentCreatedEvent event) {
    // DB 트랜잭션이 정상 커밋된 뒤 실제 파일 저장 처리함
    BinaryContent binaryContent = binaryContentRepository.findById(event.binaryContentId())
        .orElse(null);

    if (binaryContent == null) {
      log.warn("저장할 바이너리 콘텐츠를 찾을 수 없음: id={}", event.binaryContentId());
      return;
    }

    try {
      binaryContentStorage.put(event.binaryContentId(), event.bytes());

      // 실제 파일 저장 성공 시 상태 성공으로 변경함
      binaryContent.updateStatus(BinaryContentStatus.SUCCESS);
      log.info("바이너리 콘텐츠 파일 저장 완료: id={}", event.binaryContentId());
    } catch (RuntimeException e) {
      // 파일 저장 실패해도 메타데이터는 남기고 실패 상태로 변경함
      binaryContent.updateStatus(BinaryContentStatus.FAIL);
      log.error("바이너리 콘텐츠 파일 저장 실패: id={}", event.binaryContentId(), e);
    }
  }
}
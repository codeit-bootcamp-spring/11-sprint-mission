package com.sprint.mission.discodeit.listener;

import com.sprint.mission.discodeit.dto.binarycontentdto.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.entity.BinaryContent.BinaryContentStatus;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.FileException;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BinaryContentEventListener {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentService binaryContentService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleBinaryContentCreated(BinaryContentCreatedEvent event) {

    log.info("BinaryContentCreatedEvent 수신: {}", event.binaryContentId());

    try {
      binaryContentStorage.put(event.binaryContentId(), event.data());
      binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.SUCCESS);
    } catch (Exception e) {
      log.error("파일 저장 중 오류가 발생했습니다. userId: {}", event.binaryContentId(), e);
      
      throw new FileException(ErrorCode.FILE_PUT_ERROR,
          Map.of("binaryContentId", event.binaryContentId()));
    }


  }

}

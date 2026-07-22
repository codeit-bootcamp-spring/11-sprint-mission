package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;


@Slf4j
@RequiredArgsConstructor
@Component
public class BinaryContentCreatedEventListener {

    private final BinaryContentStorage binaryContentStorage;
    private final BinaryContentService binaryContentService;

    @Async
    @TransactionalEventListener
    public void on(BinaryContentCreatedEvent event) {
        log.debug("바이너리 데이터 저장 시작: id={}", event.binaryContentId());
        try {
            binaryContentStorage.put(event.binaryContentId(), event.bytes());
            binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.SUCCESS);
            log.info("바이너리 데이터 저장 완료: id={}", event.binaryContentId());
        } catch (Exception e) {
            binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.FAIL);
            log.error("바이너리 데이터 저장 실패: id={}", event.binaryContentId(), e);
        }
    }
}

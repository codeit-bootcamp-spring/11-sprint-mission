package com.sprint.mission.discodeit.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationRequiredEventListener {

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) {
    log.error("S3 업로드 실패 알림 - requestId: {}, binaryContentId: {}, 원인: {}",
        event.requestId(), event.binaryContentId(), event.errorMessage());
    // TODO: 관리자에게 알림 발송 (Kafka, 이메일 등 연동 시 확장)
  }
}

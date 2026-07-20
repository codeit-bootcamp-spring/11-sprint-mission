package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredEventListener {

  private final NotificationService notificationService;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {
    notificationService.createAll(event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    notificationService.create(event);
  }

  // 업로드 실패는 트랜잭션 밖(비동기 저장 중)에서 발생하므로 커밋 시점과 무관하게 처리한다
  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) {
    notificationService.createAll(event);
  }
}

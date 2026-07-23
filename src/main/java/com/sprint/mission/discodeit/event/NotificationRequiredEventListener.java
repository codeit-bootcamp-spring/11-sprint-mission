package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!kafka")
public class NotificationRequiredEventListener {

  private final ReadStatusRepository readStatusRepository;
  private final NotificationService notificationService;

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(MessageCreatedEvent event) {
    List<ReadStatus> targets = readStatusRepository
        .findAllByChannelIdAndNotificationEnabledTrueAndUserIdNot(
            event.channelId(), event.authorId());

    String title = event.authorName() + " (#" + event.channelName() + ")";
    targets.forEach(rs -> notificationService.create(rs.getUser().getId(), title, event.content()));
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(RoleUpdatedEvent event) {
    String content = event.oldRole().name() + " -> " + event.newRole().name();
    notificationService.create(event.userId(), "권한이 변경되었습니다.", content);
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) {
    log.error("S3 업로드 실패 알림 - requestId: {}, binaryContentId: {}, 원인: {}",
        event.requestId(), event.binaryContentId(), event.errorMessage());
  }
}

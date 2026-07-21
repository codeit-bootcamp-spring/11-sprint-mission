package com.sprint.mission.discodeit.event.notification;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Profile("!kafka")
@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

  private final NotificationService notificationService;
  private final ReadStatusRepository readStatusRepository;

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(MessageCreatedEvent event) {
    log.info("메시지 알림 이벤트 수신 - channelId: {}", event.channelId());

    List<ReadStatus> targetStatuses = readStatusRepository.findNotificationEnabledTargets(
        event.channelId(), event.authorId());

    String title = String.format("%s (#%s)", event.authorName(), event.channelName());
    String content = event.content();

    targetStatuses.forEach(
        rs -> notificationService.createNotification(rs.getUser().getId(), title, content));
    log.info("메시지 알림 생성 완료 - 발송 건수: {}", targetStatuses.size());
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(RoleUpdatedEvent event) {
    log.info("권한 변경 알림 이벤트 수신 - userId: {}", event.userId());

    String title = "권한이 변경되었습니다.";
    String content =
        event.userId() + "님의 권한이 " + event.oldRole().name() + "에서 " + event.newRole().name()
            + "으로 변경되었습니다.";

    notificationService.createNotification(event.userId(), title, content);
    log.info("권한 변경 알림 생성 완료 - userId: {}", event.userId());
  }
}

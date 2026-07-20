package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
        .findAllByChannelIdAndNotificationEnabledTrue(event.channelId());

    String title = event.authorName() + " (#" + event.channelName() + ")";

    targets.stream()
        .filter(rs -> !rs.getUser().getId().equals(event.authorId()))
        .forEach(rs -> notificationService.create(rs.getUser().getId(), title, event.content()));
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(RoleUpdatedEvent event) {
    String content = event.oldRole() + " -> " + event.newRole();
    notificationService.create(event.userId(), "권한이 변경되었습니다.", content);
  }
}

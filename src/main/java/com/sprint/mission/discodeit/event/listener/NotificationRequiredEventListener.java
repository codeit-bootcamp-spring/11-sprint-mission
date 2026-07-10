package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

  private final ReadStatusRepository readStatusRepository;
  private final NotificationRepository notificationRepository;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(MessageCreatedEvent event) {
    List<ReadStatus> targets = readStatusRepository
        .findAllByChannelIdAndNotificationEnabledTrue(event.channelId());

    String title = event.authorName() + " (#" + event.channelName() + ")";

    List<Notification> notifications = targets.stream()
        .filter(rs -> !rs.getUser().getId().equals(event.authorId()))
        .map(rs -> new Notification(rs.getUser().getId(), title, event.content()))
        .toList();

    notificationRepository.saveAll(notifications);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(RoleUpdatedEvent event) {
    String content = event.oldRole() + " -> " + event.newRole();
    Notification notification = new Notification(
        event.userId(),
        "권한이 변경되없습니다.",
        content
    );
    notificationRepository.save(notification);
  }
}

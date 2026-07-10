package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

  private final ReadStatusRepository readStatusRepository;
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {
    List<ReadStatus> readStatuses =
        readStatusRepository.findAllByChannelIdWithUser(event.channelId());

    List<Notification> notifications = readStatuses.stream()
        .filter(ReadStatus::isNotificationEnabled)
        .filter(readStatus -> !readStatus.getUser().getId().equals(event.authorId()))
        .map(readStatus -> new Notification(
            readStatus.getUser(),
            event.authorUsername() + " (#" + event.channelName() + ")",
            event.content()
        ))
        .toList();

    notificationRepository.saveAll(notifications);
  }

  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    User receiver = userRepository.findById(event.userId())
        .orElseThrow(() -> UserNotFoundException.withId(event.userId()));

    Notification notification = new Notification(
        receiver,
        "권한이 변경되었습니다.",
        event.previousRole() + " -> " + event.newRole()
    );

    notificationRepository.save(notification);
  }
}
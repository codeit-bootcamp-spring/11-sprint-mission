package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

  private final ReadStatusRepository readStatusRepository;
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void on(MessageCreatedEvent event) {
    log.info("메시지 생성 이벤트 수신 - channelId: {}", event.getChannelId());

    User author = userRepository.findById(event.getAuthorId()).orElseThrow();

    List<ReadStatus> activeReadStatuses = readStatusRepository.findByChannelIdAndNotificationEnabledTrue(
        event.getChannelId());

    List<Notification> notifications = activeReadStatuses.stream()
        .map(ReadStatus::getUser)
        .filter(user -> !user.getId().equals(event.getAuthorId()))
        .map(user -> new Notification(
            user,
            author.getUsername() + " (#" + event.getChannelName() + ")",
            event.getContent()
        )).toList();

    notificationRepository.saveAll(notifications);
    log.info("{}명에게 메시지 알림 전송 완료", notifications.size());
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void on(RoleUpdatedEvent event) {
    log.info("권한 변경 이벤트 수신 - userId: {}", event.getUserId());

    User user = userRepository.findById(event.getUserId()).orElseThrow();

    Notification notification = new Notification(
        user,
        "권한이 변경되었습니다.",
        event.getOldRole() + " -> " + event.getNewRole()
    );

    notificationRepository.save(notification);
    log.info("권한 변경 알림 전송 완료 - userId: {}", event.getUserId());
  }
}

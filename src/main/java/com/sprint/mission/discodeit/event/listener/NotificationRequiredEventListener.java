package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Kafka 도입(2.6) 이후 비활성화됨.
 * 알림 생성 책임은 {@code event.kafka.KafkaProduceRequiredEventListener}(발행)와
 * {@code event.kafka.NotificationRequiredTopicListener}(구독/알림 생성)로 이전되었다.
 * 로컬(같은 프로세스) 이벤트 처리로 되돌리려면 @Component를 다시 붙이고
 * KafkaProduceRequiredEventListener를 비활성화하면 된다.
 */
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

  private final NotificationService notificationService;
  private final ReadStatusRepository readStatusRepository;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {
    List<ReadStatus> targets = readStatusRepository
        .findAllByChannelIdAndNotificationEnabledTrue(event.channelId());

    String title = "%s (#%s)".formatted(event.authorName(), event.channelName());

    targets.stream()
        .filter(rs -> !rs.getUser().getId().equals(event.authorId()))
        .forEach(rs -> notificationService.create(rs.getUser().getId(), title, event.content()));
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    String content = "%s -> %s".formatted(event.previousRole(), event.newRole());
    notificationService.create(event.userId(), "권한이 변경되었습니다.", content);
  }
}

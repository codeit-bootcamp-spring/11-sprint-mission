package com.sprint.mission.discodeit.listener;


import com.sprint.mission.discodeit.dto.messagedto.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.dto.userdto.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.JPAReadStatusRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

  private final JPAReadStatusRepository readStatusRepository;
  private final NotificationService notificationService;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {

    List<ReadStatus> readStatuses = readStatusRepository
        .findAllByChannelIdAndNotificationEnabledTrue(event.channelId());

    readStatuses.stream()
        .filter(rs -> !rs.getUser().getId().equals(event.authorId())) // 본인 제외
        .forEach(rs -> notificationService.create(
            rs.getUser().getId(),
            event.authorUsername() + " (#" + event.channelName() + ")",
            event.content()
        ));

  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    notificationService.create(
        event.userId(),
        "권한이 변경되었습니다.",
        event.previousRole() + " -> " + event.newRole()
    );

  }

}

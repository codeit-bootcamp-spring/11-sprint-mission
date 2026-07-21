package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 알림이 필요한 이벤트(새 메시지 등록, 권한 변경)를 처리해 알림을 생성하는 리스너입니다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredEventListener {

  private final ReadStatusRepository readStatusRepository;
  private final NotificationService notificationService;

  @Async
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {
    MessageDto message = event.message();
    UUID channelId = message.channelId();
    UUID authorId = message.author().id();
    String authorUsername = message.author().username();
    String channelName = event.channelName();

    log.debug("메시지 등록 알림 생성 시작: channelId={}, authorId={}", channelId, authorId);

    String title = channelName != null
        ? "%s (#%s)".formatted(authorUsername, channelName)
        : authorUsername;

    Set<UUID> receiverIds = readStatusRepository
        .findAllByChannelIdAndNotificationEnabledTrue(channelId).stream()
        .map(ReadStatus::getUser)
        .map(user -> user.getId())
        .filter(userId -> !userId.equals(authorId))
        .collect(Collectors.toSet());

    notificationService.create(receiverIds, title, message.content());

    log.info("메시지 등록 알림 생성 완료: channelId={}, authorId={}, 수신자 수={}",
        channelId, authorId, receiverIds.size());
  }

  @Async
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    log.debug("권한 변경 알림 생성 시작: userId={}, {} -> {}",
        event.userId(), event.previousRole(), event.newRole());

    String title = "권한이 변경되었습니다.";
    String content = "%s -> %s".formatted(event.previousRole(), event.newRole());
    notificationService.create(Set.of(event.userId()), title, content);

    log.info("권한 변경 알림 생성 완료: userId={}", event.userId());
  }
}

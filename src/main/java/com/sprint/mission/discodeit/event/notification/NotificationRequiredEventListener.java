package com.sprint.mission.discodeit.event.notification;

import com.sprint.mission.discodeit.config.CacheConfig;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Profile("!kafka")
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final NotificationRepository notificationRepository;
  private final CacheManager cacheManager;
  private final ApplicationEventPublisher eventPublisher;
  private final NotificationMapper notificationMapper;

  private static final int CONTENT_PREVIEW_LENGTH = 100;

  @Async("eventTaskExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(MessageCreatedEvent event) {
    log.debug("메시지 알림 생성 시작 - channelId: {}", event.channelId());

    List<UUID> receiverIds = readStatusRepository
        .findReceiverIdsByChannelIdAndNotificationEnabledTrueExcludingAuthor(
            event.channelId(), event.authorId());
    if (receiverIds.isEmpty()) {
      log.debug("알림 대상 없음 - channelId: {}", event.channelId());
      return;
    }

    User author = userRepository.findById(event.authorId())
        .orElseThrow(() -> new UserNotFoundException(event.authorId()));

    List<User> receivers = userRepository.findAllById(receiverIds);

    // Private 채널은 채널명이 없으므로 괄호 생략
    String title = (event.channelName() != null && !event.channelName().isBlank()) ?
        String.format("%s (#%s)", author.getUsername(), event.channelName())
        : author.getUsername();

    String content = truncate(event.content());

    List<Notification> notifications = receivers.stream()
        .map(receiver -> new Notification(receiver, title, content))
        .toList();

    notificationRepository.saveAll(notifications);

    // 알림 받은 유저들의 캐시만 선택적으로 무효화
    evictNotificationsCacheForUsers(receiverIds);

    notifications.forEach(n ->
        eventPublisher.publishEvent(new NotificationCreatedEvent(notificationMapper.toDto(n)))
    );

    log.info("메시지 알림 생성 완료 - channelId: {}, 수신자 수: {}",
        event.channelId(), notifications.size());
  }

  @Async("eventTaskExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(RoleUpdatedEvent event) {
    log.debug("권한 변경 알림 생성 시작 - userId: {}", event.userId());

    User user = userRepository.findById(event.userId())
        .orElseThrow(() -> new UserNotFoundException(event.userId()));

    String title = "권한이 변경되었습니다.";
    String content = String.format("%s -> %s", event.oldRole(), event.newRole());

    Notification saved = notificationRepository.save(new Notification(user, title, content));

    // 대상 유저 캐시 무효화
    evictNotificationsCacheForUsers(List.of(event.userId()));

    eventPublisher.publishEvent(new NotificationCreatedEvent(notificationMapper.toDto(saved)));

    log.info("권한 변경 알림 생성 완료 - userId: {}", event.userId());
  }

  private String truncate(String content) {
    if (content == null) {
      return "";
    }
    return content.length() > CONTENT_PREVIEW_LENGTH
        ? content.substring(0, CONTENT_PREVIEW_LENGTH) + "..."
        : content;
  }

  private void evictNotificationsCacheForUsers(List<UUID> userIds) {
    Cache cache = cacheManager.getCache(CacheConfig.NOTIFICATIONS);
    if (cache != null) {
      userIds.forEach(cache::evict);
    }
  }

}

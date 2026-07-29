package com.sprint.mission.discodeit.event.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.config.CacheConfig;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.s3.S3UploadFailedEvent;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("kafka")
@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredTopicListener {

  private static final int CONTENT_PREVIEW_LENGTH = 100;

  private final ObjectMapper objectMapper;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final NotificationRepository notificationRepository;
  private final CacheManager cacheManager;

  @KafkaListener(topics = "discodeit.MessageCreatedEvent")
  public void onMessageCreatedEvent(String kafkaEvent) {
    try {
      MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);
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

      String title = (event.channelName() != null && !event.channelName().isBlank()) ?
          String.format("%s (#%s)", author.getUsername(), event.channelName())
          : author.getUsername();

      String content = truncate(event.content());

      List<Notification> notifications = receivers.stream()
          .map(receiver -> new Notification(receiver, title, content))
          .toList();

      notificationRepository.saveAll(notifications);

      evictNotificationsCacheForUsers(receiverIds);

      log.info("메시지 알림 생성 완료 - channelId: {}, 수신자 수: {}", event.channelId(), notifications.size());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Transactional
  @KafkaListener(topics = "discodeit.RoleUpdatedEvent")
  public void onRoleUpdatedEvent(String kafkaEvent) {
    try {
      RoleUpdatedEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedEvent.class);
      log.debug("권한 변경 알림 생성 시작 - userId: {}", event.userId());

      User user = userRepository.findById(event.userId())
          .orElseThrow(() -> new UserNotFoundException(event.userId()));

      String title = "권한이 변경되었습니다.";
      String content = String.format("%s -> %s", event.oldRole(), event.newRole());

      notificationRepository.save(new Notification(user, title, content));

      evictNotificationsCacheForUsers(List.of(event.userId()));

      log.info("권한 변경 알림 생성 완료 - userId: {}", event.userId());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "discodeit.S3UploadFailedEvent")
  public void onS3UploadFailedEvent(String kafkaEvent) {
    try {
      S3UploadFailedEvent event = objectMapper.readValue(kafkaEvent, S3UploadFailedEvent.class);
      log.error("""
          [관리자 알림] 비동기 작업 실패
          작업: {}
          RequestId: {}
          BinaryContentId: {}
          Error: {}
          """, event.taskName(), event.requestId(), event.binaryContentId(), event.errorMessage());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
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

package com.sprint.mission.discodeit.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRequiredTopicListener {

  private final ObjectMapper objectMapper;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  @KafkaListener(topics = "discodeit.MessageCreatedEvent")
  public void onMessageCreatedEvent(String kafkaEvent) {
    try {
      MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);

      List<ReadStatus> targets = readStatusRepository
          .findAllByChannelIdAndNotificationEnabledTrue(event.channelId());

      String title = event.authorName() + " (#" + event.channelName() + ")";

      targets.stream()
          .filter(rs -> !rs.getUser().getId().equals(event.authorId()))
          .forEach(rs -> notificationService.create(rs.getUser().getId(), title, event.content()));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "discodeit.RoleUpdatedEvent")
  public void onRoleUpdatedEvent(String kafkaEvent) {
    try {
      RoleUpdatedEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedEvent.class);
      String content = event.oldRole() + " -> " + event.newRole();
      notificationService.create(event.userId(), "권한이 변경되었습니다.", content);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "discodeit.S3UploadFailedEvent")
  public void onS3UploadFailedEvent(String kafkaEvent) {
    try {
      S3UploadFailedEvent event = objectMapper.readValue(kafkaEvent, S3UploadFailedEvent.class);

      List<User> admins = userRepository.findAllByRole(Role.ADMIN);
      String title = "S3 업로드 실패";
      String content = String.format(
          "requestId: %s, binaryContentId: %s, error: %s",
          event.requestId(), event.binaryContentId(), event.errorMessage()
      );

      if (admins.isEmpty()) {
        log.warn("S3 업로드 실패 알림을 받을 관리자가 없습니다 - requestId: {}", event.requestId());
        return;
      }

      admins.forEach(admin -> notificationService.create(admin.getId(), title, content));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}

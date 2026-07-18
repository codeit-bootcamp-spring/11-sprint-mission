package com.sprint.mission.discodeit.event.kafka;

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

/**
 * Kafka 토픽을 구독해 알림을 생성하는 리스너.
 * 메인 서비스와 분리된 별도의 "알림 서비스"라고 가정한다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredTopicListener {

  private final ObjectMapper objectMapper;
  private final NotificationService notificationService;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;

  @KafkaListener(topics = "discodeit.MessageCreatedEvent")
  public void onMessageCreatedEvent(String kafkaEvent) {
    try {
      MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);

      List<ReadStatus> targets = readStatusRepository
          .findAllByChannelIdAndNotificationEnabledTrue(event.channelId());

      String title = "%s (#%s)".formatted(event.authorName(), event.channelName());

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
      String content = "%s -> %s".formatted(event.previousRole(), event.newRole());
      notificationService.create(event.userId(), "권한이 변경되었습니다.", content);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "discodeit.S3UploadFailedEvent")
  public void onS3UploadFailedEvent(String kafkaEvent) {
    try {
      S3UploadFailedEvent event = objectMapper.readValue(kafkaEvent, S3UploadFailedEvent.class);
      String content = """
          RequestId: %s
          BinaryContentId: %s
          Error: %s
          """.formatted(event.requestId(), event.binaryContentId(), event.errorMessage());
      String title = "파일 업로드 실패: " + event.taskName();

      for (User admin : userRepository.findAllByRole(Role.ADMIN)) {
        notificationService.create(admin.getId(), title, content);
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}

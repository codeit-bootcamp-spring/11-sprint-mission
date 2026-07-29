package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.dto.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.dto.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.dto.S3UploadFailedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.Role;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("kafka")
@RequiredArgsConstructor
public class NotificationRequiredTopicListener {

  private final ObjectMapper objectMapper;
  private final NotificationService notificationService;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;

  @KafkaListener(topics = "discodeit.MessageCreatedEvent", groupId = "discodeit-group")
  public void onMessageCreatedEvent(String kafkaEvent) {
    try {
      MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);

      log.info("Kafka 메시지 알림 이벤트 수신 - channelId: {}", event.data().channelId());

      List<ReadStatus> targetStatuses = readStatusRepository.findNotificationEnabledTargets(
          event.data().channelId(), event.data().author().id());

      String title = String.format("%s (#%s)", event.data().author().username(),
          event.channelName());
      String content = event.data().content();

      targetStatuses.forEach(
          rs -> notificationService.createNotification(rs.getUser().getId(), title, content));

      log.info("Kafka 메시지 알림 생성 완료 - 발송 건수: {}", targetStatuses.size());
    } catch (JsonProcessingException e) {
      log.error("Kafka 메시지 알림 발송 실패", e);
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "discodeit.RoleUpdatedEvent", groupId = "discodeit-group")
  public void onRoleUpdatedEvent(String kafkaEvent) {
    try {
      RoleUpdatedEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedEvent.class);

      log.info("Kafka 권한 변경 알림 이벤트 수신 - userId: {}", event.userId());

      String title = "권한이 변경되었습니다.";
      String content =
          event.userId() + "님의 권한이 " + event.oldRole().name() + "에서 " + event.newRole().name()
              + "으로 변경되었습니다.";

      notificationService.createNotification(event.userId(), title, content);
      log.info("Kafka 권한 변경 알림 생성 완료 - userId: {}", event.userId());
    } catch (JsonProcessingException e) {
      log.error("Kafka 권한 변경 알림 발송 실패", e);
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "discodeit.S3UploadFailedEvent", groupId = "discodeit-group")
  public void onS3UploadFailedEvent(String kafkaEvent) {
    try {
      S3UploadFailedEvent event = objectMapper.readValue(kafkaEvent, S3UploadFailedEvent.class);

      log.info("Kafka S3 업로드 실패 이벤트 수신. 관리자 알림 발송 시작");

      List<User> admins = userRepository.findAllByRole(Role.ADMIN);

      if (admins.isEmpty()) {
        log.error("Kafka 알림을 받을 관리자(admin) 계정이 DB에 존재하지 않습니다.");
        return;
      }

      String title = "S3 파일 업로드 실패";
      String content = String.format("RequestId: %s\nBinaryContentId: %s\nError: %s",
          event.requestId(), event.binaryContentId(), event.errorMessage());

      admins.forEach(
          admin -> notificationService.createNotification(admin.getId(), title, content));

      log.info("Kafka 관리자 알림 생성 완료 - 발송 건수: {}", admins.size());
    } catch (JsonProcessingException e) {
      log.error("Kafka S3 업로드 실패 알림 발송 실패", e);
      throw new RuntimeException(e);
    }
  }
}

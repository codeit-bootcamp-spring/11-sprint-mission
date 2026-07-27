package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.User.Role;
import com.sprint.mission.discodeit.event.notification.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.notification.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.notification.S3UploadFailedEvent;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRequiredTopicListener {

  private final ReadStatusRepository readStatusRepository;
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;
  private final ChannelService channelService;

  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = "discodeit.MessageCreatedEvent",
      groupId = "${spring.application.name}-notification")
  public void onMessageCreatedEvent(String kafkaEvent) {

    try {
      // Json → Java 객체로 역직렬화
      MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);

      MessageDto message = event.getData();
      ChannelDto channel = channelService.find(message.channelId());

      // 알림이 활성화된 ChannelId로 ReadStatus를 조회
      List<ReadStatus> readStatuses =
          readStatusRepository.findByChannelIdAndNotificationEnabledTrue(
              message.channelId());

      for (ReadStatus readStatus : readStatuses) {

        // 메시지 작성자는 제외
        if (readStatus.getUser().getId().equals(message.author().id())) {
          continue;
        }

        // 알림 생성
        notificationService.create(
            readStatus.getUser().getId(),
            message.author().username() + " #(" + channel.name() + ")",
            message.content()
        );

        // Caffeine 캐시는 로컬 캐시이기 때문에 서버마다 독립된 메모리를 사용
        // Kafka 처럼 별도의 서버에서는 Caffeine 캐시를 사용할 수 없음
        // TODO : Redis Cache를 도입
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

  }

  @KafkaListener(
      topics = "discodeit.RoleUpdatedEvent",
      groupId = "${spring.application.name}-notification")
  public void onRoleUpdatedEvent(String kafkaEvent) {

    try {
      // Json → Java 객체로 역직렬화
      RoleUpdatedEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedEvent.class);

      User user = userRepository.findById(event.getUserId()).orElseThrow(
          () -> new UserNotFoundException(event.getUserId())
      );

      notificationService.create(
          user.getId(),
          "권한이 변경되었습니다.",
          event.getFrom().name() + " -> " + event.getTo().name()
      );

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

  }

  @KafkaListener(
      topics = "discodeit.S3UploadFailedEvent",
      groupId = "${spring.application.name}-notification")
  public void onS3UploadFailedEvent(String kafkaEvent) {

    try {
      // Json → Java 객체로 역직렬화
      S3UploadFailedEvent event = objectMapper.readValue(kafkaEvent, S3UploadFailedEvent.class);

      // 관리자가 2명 이상일수도 있기 때문에 find가 아닌 findAll을 사용(List 타입)
      List<User> admins = userRepository.findAllByRole(Role.ADMIN);

      // 관리자가 없을 때
      if (admins.isEmpty()) {
        log.error("관리자 계정이 존재하지 않습니다. error={}", event.e().getMessage());
        return;
      }

      // 알림 메시지 제목
      String title = "S3 파일 업로드 실패";

      String requestId = MDC.get("requestId");

      // 알림 메시지 포맷
      String message = """
          RequestId: %s
          BinaryContentId: %s
          Error: %s
          """.formatted(requestId, event.binaryContentId(), event.e().getMessage());

      // List<User> → List<Notification>
      List<Notification> notifications = admins.stream()
          .map(admin -> Notification.create(admin, title, message))
          .toList();

      notificationRepository.saveAll(notifications);

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

  }

}

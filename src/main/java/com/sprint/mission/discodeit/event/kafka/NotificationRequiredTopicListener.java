package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
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

  private final ReadStatusRepository readStatusRepository;
  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = KafkaTopics.MESSAGE_CREATED, groupId = "${spring.kafka.consumer.group-id}")
  public void onMessageCreated(String payload) {
    try {
      MessageCreatedMessage message = objectMapper.readValue(payload, MessageCreatedMessage.class);
      List<ReadStatus> targets = readStatusRepository
          .findAllByChannelIdAndNotificationEnabledTrueAndUserIdNot(
              message.channelId(), message.authorId());

      String title = message.authorName() + " (#" + message.channelName() + ")";
      targets.forEach(readStatus ->
          notificationService.create(readStatus.getUser().getId(), title, message.content()));
    } catch (JsonProcessingException e) {
      log.error("Kafka 메시지 역직렬화 실패: topic={}", KafkaTopics.MESSAGE_CREATED, e);
    }
  }

  @KafkaListener(topics = KafkaTopics.ROLE_UPDATED, groupId = "${spring.kafka.consumer.group-id}")
  public void onRoleUpdated(String payload) {
    try {
      RoleUpdatedMessage message = objectMapper.readValue(payload, RoleUpdatedMessage.class);
      String content = message.oldRole().name() + " -> " + message.newRole().name();
      notificationService.create(message.userId(), "권한이 변경되었습니다.", content);
    } catch (JsonProcessingException e) {
      log.error("Kafka 메시지 역직렬화 실패: topic={}", KafkaTopics.ROLE_UPDATED, e);
    }
  }
}

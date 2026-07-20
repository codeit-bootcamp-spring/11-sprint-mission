package com.sprint.mission.discodeit.event.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KafkaEventSerializationTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DisplayName("MessageCreatedEvent JSON 왕복 변환")
  void messageCreatedEvent_RoundTrip() throws Exception {
    MessageCreatedEvent event = new MessageCreatedEvent(
        UUID.randomUUID(), UUID.randomUUID(), "공지", UUID.randomUUID(), "author", "안녕하세요");

    String payload = objectMapper.writeValueAsString(event);

    assertThat(objectMapper.readValue(payload, MessageCreatedEvent.class)).isEqualTo(event);
  }

  @Test
  @DisplayName("RoleUpdatedEvent JSON 왕복 변환")
  void roleUpdatedEvent_RoundTrip() throws Exception {
    RoleUpdatedEvent event =
        new RoleUpdatedEvent(UUID.randomUUID(), Role.USER, Role.CHANNEL_MANAGER);

    String payload = objectMapper.writeValueAsString(event);

    assertThat(objectMapper.readValue(payload, RoleUpdatedEvent.class)).isEqualTo(event);
  }

  @Test
  @DisplayName("S3UploadFailedEvent JSON 왕복 변환")
  void s3UploadFailedEvent_RoundTrip() throws Exception {
    S3UploadFailedEvent event = new S3UploadFailedEvent(
        "S3 파일 업로드", "test-request-id", UUID.randomUUID(), "Access Denied");

    String payload = objectMapper.writeValueAsString(event);

    assertThat(objectMapper.readValue(payload, S3UploadFailedEvent.class)).isEqualTo(event);
  }
}
package com.sprint.mission.discodeit.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.data.ReadStatusDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.event.kafka.KafkaTopic;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.NotificationService;
import com.sprint.mission.discodeit.service.ReadStatusService;
import com.sprint.mission.discodeit.service.UserService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.listener.auto-startup=true"
})
@EmbeddedKafka(partitions = 1, topics = {
    KafkaTopic.MESSAGE_CREATED, KafkaTopic.ROLE_UPDATED, KafkaTopic.S3_UPLOAD_FAILED
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@WithMockUser(roles = "CHANNEL_MANAGER")
class NotificationApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserService userService;

  @Autowired
  private ChannelService channelService;

  @Autowired
  private MessageService messageService;

  @Autowired
  private ReadStatusService readStatusService;

  @Autowired
  private NotificationService notificationService;

  private UserDto author;
  private UserDto receiver;
  private DiscodeitUserDetails authorDetails;
  private DiscodeitUserDetails receiverDetails;
  private ChannelDto channel;

  @BeforeEach
  void setUp() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    author = userService.create(
        new UserCreateRequest("author" + suffix, "author" + suffix + "@example.com", "Password1!"),
        Optional.empty());
    receiver = userService.create(
        new UserCreateRequest("receiver" + suffix, "receiver" + suffix + "@example.com",
            "Password1!"),
        Optional.empty());
    authorDetails = new DiscodeitUserDetails(author, "Password1!");
    receiverDetails = new DiscodeitUserDetails(receiver, "Password1!");

    channel = channelService.create(new PublicChannelCreateRequest("공지", "공지 채널입니다."));

    // PUBLIC 채널의 읽음 상태는 알림이 비활성화된 상태로 생성되므로 수신자만 활성화한다
    enableNotification(author.id(), false);
    enableNotification(receiver.id(), true);
  }

  private void enableNotification(UUID userId, boolean enabled) {
    ReadStatusDto readStatus = readStatusService.create(
        new ReadStatusCreateRequest(userId, channel.id(), Instant.now()));
    assertThat(readStatus.notificationEnabled()).isFalse();
    if (enabled) {
      readStatusService.update(readStatus.id(), new ReadStatusUpdateRequest(null, true));
    }
  }

  @Test
  @DisplayName("메시지 생성 시 알림을 활성화한 사용자에게 알림이 생성된다")
  void findAllNotifications_AfterMessageCreated_Success() throws Exception {
    // Given
    messageService.create(
        new MessageCreateRequest("안녕하세요", channel.id(), author.id()), new ArrayList<>());
    awaitOnlyNotificationId();

    // When & Then
    mockMvc.perform(get("/api/notifications").with(user(receiverDetails)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].receiverId", is(receiver.id().toString())))
        .andExpect(jsonPath("$[0].title", is(author.username() + " (#공지)")))
        .andExpect(jsonPath("$[0].content", is("안녕하세요")));

    // 메시지를 보낸 사람은 알림 대상에서 제외된다
    mockMvc.perform(get("/api/notifications").with(user(authorDetails)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @DisplayName("인증되지 않은 사용자의 알림 목록 조회는 401을 반환한다")
  void findAllNotifications_Unauthenticated_Returns401() throws Exception {
    mockMvc.perform(get("/api/notifications").with(anonymous()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("본인의 알림 확인 성공")
  void deleteNotification_Success() throws Exception {
    // Given
    messageService.create(
        new MessageCreateRequest("확인할 알림", channel.id(), author.id()), new ArrayList<>());
    UUID notificationId = awaitOnlyNotificationId();

    // When & Then
    mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId)
            .with(csrf())
            .with(user(receiverDetails)))
        .andExpect(status().isNoContent());

    assertThat(notificationService.findAllByReceiverId(receiver.id())).isEmpty();
  }

  @Test
  @DisplayName("타인의 알림 확인 시 403을 반환한다")
  void deleteNotification_NotOwner_Returns403() throws Exception {
    // Given
    messageService.create(
        new MessageCreateRequest("남의 알림", channel.id(), author.id()), new ArrayList<>());
    UUID notificationId = awaitOnlyNotificationId();

    // When & Then
    mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId)
            .with(csrf())
            .with(user(authorDetails)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("존재하지 않는 알림 확인 시 404를 반환한다")
  void deleteNotification_NonExistent_Returns404() throws Exception {
    mockMvc.perform(delete("/api/notifications/{notificationId}", UUID.randomUUID())
            .with(csrf())
            .with(user(receiverDetails)))
        .andExpect(status().isNotFound());
  }

  // 알림은 비동기로 생성되므로 생성될 때까지 기다린다
  private UUID awaitOnlyNotificationId() {
    await().atMost(Duration.ofSeconds(20))
        .untilAsserted(() -> assertThat(notificationService.findAllByReceiverId(receiver.id()))
            .hasSize(1));
    return notificationService.findAllByReceiverId(receiver.id()).get(0).id();
  }
}
package com.sprint.mission.discodeit.integration;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.dto.data.ReadStatusDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.NotificationService;
import com.sprint.mission.discodeit.service.ReadStatusService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.util.AsyncTestUtils;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class NotificationApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

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

  @Test
  @WithMockUser(roles = "CHANNEL_MANAGER")
  @DisplayName("새 메시지 등록 시 알림이 켜진 구독자에게 알림이 생성되고, 작성자는 제외된다")
  void messageCreated_NotifiesSubscriber_ExcludesAuthor() throws Exception {
    // Given
    UserDto author = userService.create(
        new UserCreateRequest("msgauthor", "msgauthor@example.com", "Password1!"),
        Optional.empty());
    UserDto subscriber = userService.create(
        new UserCreateRequest("msgsubscriber", "msgsubscriber@example.com", "Password1!"),
        Optional.empty());

    ChannelDto channel = channelService.create(
        new PublicChannelCreateRequest("알림 테스트 채널", "설명"));

    // 구독자: PUBLIC 채널은 기본 알림 비활성화이므로 명시적으로 활성화
    ReadStatusDto subscriberReadStatus = readStatusService.create(
        new ReadStatusCreateRequest(subscriber.id(), channel.id(), Instant.now()));
    readStatusService.update(subscriberReadStatus.id(),
        new ReadStatusUpdateRequest(null, true));

    // 작성자 자신도 읽음 상태를 가지지만 알림 대상에서는 제외되어야 함
    ReadStatusDto authorReadStatus = readStatusService.create(
        new ReadStatusCreateRequest(author.id(), channel.id(), Instant.now()));
    readStatusService.update(authorReadStatus.id(), new ReadStatusUpdateRequest(null, true));

    MessageDto message = messageService.create(
        new MessageCreateRequest("알림 테스트 메시지입니다.", channel.id(), author.id()),
        new ArrayList<>());

    // 메시지 등록 이벤트 리스너(AFTER_COMMIT)가 실행되도록 트랜잭션을 강제 커밋
    TestTransaction.flagForCommit();
    TestTransaction.end();
    TestTransaction.start();

    // 리스너가 @Async로 별도 스레드에서 처리되므로, 구독자에게 알림이 생성될 때까지 기다립니다.
    AsyncTestUtils.awaitUntil(
        () -> notificationService.findAllByReceiverId(subscriber.id()).stream()
            .anyMatch(n -> "알림 테스트 메시지입니다.".equals(n.content())),
        5000
    );

    DiscodeitUserDetails subscriberDetails = new DiscodeitUserDetails(subscriber, "Password1!");
    DiscodeitUserDetails authorDetails = new DiscodeitUserDetails(author, "Password1!");

    // When & Then - 구독자는 알림을 받는다
    mockMvc.perform(get("/api/notifications")
            .with(user(subscriberDetails)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].title", hasItem("msgauthor (#알림 테스트 채널)")))
        .andExpect(jsonPath("$[*].content", hasItem("알림 테스트 메시지입니다.")));

    // 작성자 본인은 이 메시지에 대한 알림을 받지 않는다
    List<NotificationDto> authorNotifications = notificationService.findAllByReceiverId(
        author.id());
    boolean authorGotOwnMessageNotification = authorNotifications.stream()
        .anyMatch(n -> "알림 테스트 메시지입니다.".equals(n.content()));
    org.assertj.core.api.Assertions.assertThat(authorGotOwnMessageNotification).isFalse();
    org.assertj.core.api.Assertions.assertThat(message.id()).isNotNull();
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("권한 변경 시 당사자에게 알림이 생성된다")
  void roleUpdated_NotifiesUser() throws Exception {
    // Given
    UserDto user = userService.create(
        new UserCreateRequest("roleuser", "roleuser@example.com", "Password1!"),
        Optional.empty());

    RoleUpdateRequest roleUpdateRequest = new RoleUpdateRequest(user.id(), Role.CHANNEL_MANAGER);

    // When
    mockMvc.perform(put("/api/auth/role")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(roleUpdateRequest))
            .with(csrf()))
        .andExpect(status().isOk());

    TestTransaction.flagForCommit();
    TestTransaction.end();
    TestTransaction.start();

    // 리스너가 @Async로 별도 스레드에서 처리되므로, 알림이 생성될 때까지 기다립니다.
    AsyncTestUtils.awaitUntil(
        () -> notificationService.findAllByReceiverId(user.id()).stream()
            .anyMatch(n -> "권한이 변경되었습니다.".equals(n.title())),
        5000
    );

    DiscodeitUserDetails userDetails = new DiscodeitUserDetails(user, "Password1!");

    // Then
    mockMvc.perform(get("/api/notifications")
            .with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].title", hasItem("권한이 변경되었습니다.")))
        .andExpect(jsonPath("$[*].content", hasItem("USER -> CHANNEL_MANAGER")));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("알림 확인(삭제) API 통합 테스트 - 본인 알림만 삭제 가능")
  void deleteNotification_OnlyOwnNotification() throws Exception {
    // Given
    UserDto owner = userService.create(
        new UserCreateRequest("notiowner", "notiowner@example.com", "Password1!"),
        Optional.empty());
    UserDto other = userService.create(
        new UserCreateRequest("notiother", "notiother@example.com", "Password1!"),
        Optional.empty());

    NotificationDto notification = notificationService.create(Set.of(owner.id()), "제목", "내용")
        .get(0);

    DiscodeitUserDetails otherDetails = new DiscodeitUserDetails(other, "Password1!");
    DiscodeitUserDetails ownerDetails = new DiscodeitUserDetails(owner, "Password1!");

    // When & Then - 본인이 아니면 403
    mockMvc.perform(delete("/api/notifications/{notificationId}", notification.id())
            .with(csrf())
            .with(user(otherDetails)))
        .andExpect(status().isForbidden());

    // When & Then - 본인이면 204
    mockMvc.perform(delete("/api/notifications/{notificationId}", notification.id())
            .with(csrf())
            .with(user(ownerDetails)))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(roles = "USER")
  @DisplayName("알림 확인(삭제) 실패 API 통합 테스트 - 존재하지 않는 알림")
  void deleteNotification_Failure_NotFound() throws Exception {
    // Given
    UserDto user = userService.create(
        new UserCreateRequest("notinotfound", "notinotfound@example.com", "Password1!"),
        Optional.empty());
    DiscodeitUserDetails userDetails = new DiscodeitUserDetails(user, "Password1!");
    UUID nonExistentId = UUID.randomUUID();

    // When & Then
    mockMvc.perform(delete("/api/notifications/{notificationId}", nonExistentId)
            .with(csrf())
            .with(user(userDetails)))
        .andExpect(status().isNotFound());
  }
}

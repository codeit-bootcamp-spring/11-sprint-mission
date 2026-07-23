package com.sprint.mission.discodeit.event.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationRequiredTopicListenerTest {

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private NotificationService notificationService;

  @Spy
  private ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks
  private NotificationRequiredTopicListener listener;

  @Test
  @DisplayName("메시지 생성 시 알림 활성화된 구독자들에게 알림이 생성된다")
  void onMessageCreated_NotifiesSubscribers() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    UUID subscriber1Id = UUID.randomUUID();
    UUID subscriber2Id = UUID.randomUUID();

    String payload = objectMapper.writeValueAsString(
        new MessageCreatedEvent(channelId, authorId, "작성자", "메시지 내용", "일반채널"));

    ReadStatus rs1 = mockReadStatusWithUserId(subscriber1Id);
    ReadStatus rs2 = mockReadStatusWithUserId(subscriber2Id);
    given(readStatusRepository.findAllByChannelIdAndNotificationEnabledTrueAndUserIdNot(
        channelId, authorId)).willReturn(List.of(rs1, rs2));

    // when
    listener.onMessageCreated(payload);

    // then
    String expectedTitle = "작성자 (#일반채널)";
    verify(notificationService).create(subscriber1Id, expectedTitle, "메시지 내용");
    verify(notificationService).create(subscriber2Id, expectedTitle, "메시지 내용");
    verify(notificationService, times(2)).create(any(), eq(expectedTitle), eq("메시지 내용"));
  }

  @Test
  @DisplayName("메시지 생성 시 알림 구독자가 없으면 알림이 생성되지 않는다")
  void onMessageCreated_NoSubscribers_NoNotifications() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();

    String payload = objectMapper.writeValueAsString(
        new MessageCreatedEvent(channelId, authorId, "작성자", "메시지 내용", "일반채널"));

    given(readStatusRepository.findAllByChannelIdAndNotificationEnabledTrueAndUserIdNot(
        channelId, authorId)).willReturn(List.of());

    // when
    listener.onMessageCreated(payload);

    // then
    verify(notificationService, never()).create(any(), any(), any());
  }

  @Test
  @DisplayName("알림 제목은 '작성자명 (#채널명)' 형식으로 만들어진다")
  void onMessageCreated_TitleFormat() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    UUID subscriberId = UUID.randomUUID();

    String payload = objectMapper.writeValueAsString(
        new MessageCreatedEvent(channelId, authorId, "홍길동", "안녕하세요", "공지채널"));

    ReadStatus rs = mockReadStatusWithUserId(subscriberId);
    given(readStatusRepository.findAllByChannelIdAndNotificationEnabledTrueAndUserIdNot(
        channelId, authorId)).willReturn(List.of(rs));

    // when
    listener.onMessageCreated(payload);

    // then
    verify(notificationService).create(subscriberId, "홍길동 (#공지채널)", "안녕하세요");
  }

  @Test
  @DisplayName("권한 변경 이벤트 발생 시 대상 유저에게 알림이 생성된다")
  void onRoleUpdated_CreatesNotification() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    String payload = objectMapper.writeValueAsString(
        new RoleUpdatedEvent(userId, Role.USER, Role.ADMIN));

    // when
    listener.onRoleUpdated(payload);

    // then
    verify(notificationService).create(userId, "권한이 변경되었습니다.", "USER -> ADMIN");
  }

  @Test
  @DisplayName("권한 변경 알림 내용은 '이전권한 -> 새권한' 형식이다")
  void onRoleUpdated_ContentFormat() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    String payload = objectMapper.writeValueAsString(
        new RoleUpdatedEvent(userId, Role.ADMIN, Role.USER));

    // when
    listener.onRoleUpdated(payload);

    // then
    verify(notificationService).create(userId, "권한이 변경되었습니다.", "ADMIN -> USER");
  }

  private ReadStatus mockReadStatusWithUserId(UUID userId) {
    User user = mock(User.class);
    given(user.getId()).willReturn(userId);
    ReadStatus readStatus = mock(ReadStatus.class);
    given(readStatus.getUser()).willReturn(user);
    return readStatus;
  }
}

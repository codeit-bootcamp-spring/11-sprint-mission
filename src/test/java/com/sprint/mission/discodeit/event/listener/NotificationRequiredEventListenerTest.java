package com.sprint.mission.discodeit.event.listener;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationRequiredEventListenerTest {

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private NotificationRequiredEventListener listener;

  @Test
  @DisplayName("메시지 등록 시 알림이 켜진 구독자에게만 알림을 생성하고, 작성자 본인은 제외한다")
  void onMessageCreatedEvent_NotifiesSubscribersExceptAuthor() {
    // given
    UUID channelId = UUID.randomUUID();
    Channel channel = new Channel(ChannelType.PUBLIC, "공지", "설명");
    ReflectionTestUtils.setField(channel, "id", channelId);

    User author = new User("author", "author@example.com", "password", null);
    ReflectionTestUtils.setField(author, "id", UUID.randomUUID());

    User subscriber = new User("subscriber", "subscriber@example.com", "password", null);
    ReflectionTestUtils.setField(subscriber, "id", UUID.randomUUID());

    // 작성자 본인도 알림이 켜진 구독자 목록에 포함되어 있을 수 있음 (제외되어야 함)
    ReadStatus authorReadStatus = new ReadStatus(author, channel, Instant.now());
    authorReadStatus.updateNotificationEnabled(true);
    ReadStatus subscriberReadStatus = new ReadStatus(subscriber, channel, Instant.now());
    subscriberReadStatus.updateNotificationEnabled(true);

    given(readStatusRepository.findAllByChannelIdAndNotificationEnabledTrue(eq(channelId)))
        .willReturn(List.of(authorReadStatus, subscriberReadStatus));

    UserDto authorDto = new UserDto(author.getId(), "author", "author@example.com", null, true,
        Role.USER);
    MessageDto messageDto = new MessageDto(UUID.randomUUID(), Instant.now(), Instant.now(),
        "안녕하세요", channelId, authorDto, List.of());
    MessageCreatedEvent event = new MessageCreatedEvent(messageDto, "공지");

    // when
    listener.on(event);

    // then
    verify(notificationService, times(1)).create(eq(subscriber.getId()),
        eq("author (#공지)"), eq("안녕하세요"));
    verify(notificationService, never()).create(eq(author.getId()), org.mockito.ArgumentMatchers.anyString(),
        org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  @DisplayName("권한 변경 시 당사자에게 알림을 생성한다")
  void onRoleUpdatedEvent_NotifiesUser() {
    // given
    UUID userId = UUID.randomUUID();
    RoleUpdatedEvent event = new RoleUpdatedEvent(userId, Role.USER, Role.CHANNEL_MANAGER);

    // when
    listener.on(event);

    // then
    verify(notificationService).create(eq(userId), eq("권한이 변경되었습니다."),
        eq("USER -> CHANNEL_MANAGER"));
  }
}

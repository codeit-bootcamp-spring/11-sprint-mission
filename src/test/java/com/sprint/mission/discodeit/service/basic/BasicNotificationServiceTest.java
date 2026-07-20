package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BasicNotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private NotificationMapper notificationMapper;

  @InjectMocks
  private BasicNotificationService notificationService;

  private User author;
  private User receiver;
  private Channel channel;

  @BeforeEach
  void setUp() {
    author = new User("author", "author@example.com", "Password1!", null);
    ReflectionTestUtils.setField(author, "id", UUID.randomUUID());
    receiver = new User("receiver", "receiver@example.com", "Password1!", null);
    ReflectionTestUtils.setField(receiver, "id", UUID.randomUUID());
    channel = new Channel(ChannelType.PUBLIC, "공지", "공지 채널입니다.");
    ReflectionTestUtils.setField(channel, "id", UUID.randomUUID());
  }

  @Test
  @DisplayName("메시지 알림은 알림을 활성화한 사용자에게만 생성된다")
  void createAll_MessageCreatedEvent_Success() {
    // given
    MessageCreatedEvent event = new MessageCreatedEvent(
        UUID.randomUUID(), channel.getId(), channel.getName(),
        author.getId(), author.getUsername(), "안녕하세요"
    );
    given(readStatusRepository.findAllByChannelIdAndNotificationEnabledTrue(eq(channel.getId())))
        .willReturn(List.of(
            new ReadStatus(receiver, channel, Instant.now()),
            new ReadStatus(author, channel, Instant.now())
        ));

    // when
    notificationService.createAll(event);

    // then
    ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
    verify(notificationRepository).saveAll(captor.capture());

    List<Notification> notifications = captor.getValue();
    assertThat(notifications).hasSize(1);
    assertThat(notifications.get(0).getReceiver()).isEqualTo(receiver);
    assertThat(notifications.get(0).getTitle()).isEqualTo("author (#공지)");
    assertThat(notifications.get(0).getContent()).isEqualTo("안녕하세요");
  }

  @Test
  @DisplayName("권한 변경 알림은 권한이 변경된 당사자에게만 생성된다")
  void create_RoleUpdatedEvent_Success() {
    // given
    RoleUpdatedEvent event = new RoleUpdatedEvent(receiver.getId(), Role.USER,
        Role.CHANNEL_MANAGER);
    given(userRepository.findById(eq(receiver.getId()))).willReturn(Optional.of(receiver));

    // when
    notificationService.create(event);

    // then
    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());

    Notification notification = captor.getValue();
    assertThat(notification.getReceiver()).isEqualTo(receiver);
    assertThat(notification.getTitle()).isEqualTo("권한이 변경되었습니다.");
    assertThat(notification.getContent()).isEqualTo("USER -> CHANNEL_MANAGER");
  }

  @Test
  @DisplayName("사용자별 알림 목록 조회 성공")
  void findAllByReceiverId_Success() {
    // given
    Notification notification = new Notification(receiver, "제목", "내용");
    UUID notificationId = UUID.randomUUID();
    ReflectionTestUtils.setField(notification, "id", notificationId);
    NotificationDto dto = new NotificationDto(notificationId, Instant.now(), receiver.getId(),
        "제목", "내용");

    given(notificationRepository.findAllByReceiverId(eq(receiver.getId())))
        .willReturn(List.of(notification));
    given(notificationMapper.toDto(eq(notification))).willReturn(dto);

    // when
    List<NotificationDto> result = notificationService.findAllByReceiverId(receiver.getId());

    // then
    assertThat(result).containsExactly(dto);
  }

  @Test
  @DisplayName("알림 삭제 성공")
  void delete_Success() {
    // given
    UUID notificationId = UUID.randomUUID();
    given(notificationRepository.existsById(eq(notificationId))).willReturn(true);

    // when
    notificationService.delete(notificationId);

    // then
    verify(notificationRepository).deleteById(notificationId);
  }

  @Test
  @DisplayName("존재하지 않는 알림 삭제 시 예외 발생")
  void delete_WithNonExistentId_ThrowsException() {
    // given
    UUID notificationId = UUID.randomUUID();
    given(notificationRepository.existsById(eq(notificationId))).willReturn(false);

    // when & then
    assertThatThrownBy(() -> notificationService.delete(notificationId))
        .isInstanceOf(NotificationNotFoundException.class);
  }

  @Test
  @DisplayName("존재하지 않는 알림 조회 시 예외 발생")
  void find_WithNonExistentId_ThrowsException() {
    // given
    UUID notificationId = UUID.randomUUID();
    given(notificationRepository.findById(eq(notificationId))).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> notificationService.find(notificationId))
        .isInstanceOf(NotificationNotFoundException.class);
  }
}

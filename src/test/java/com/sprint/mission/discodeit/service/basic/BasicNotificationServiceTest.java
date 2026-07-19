package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BasicNotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private NotificationMapper notificationMapper;

  @InjectMocks
  private BasicNotificationService notificationService;

  private UUID receiverId;
  private User receiver;
  private UUID notificationId;
  private Notification notification;
  private NotificationDto notificationDto;

  @BeforeEach
  void setUp() {
    receiverId = UUID.randomUUID();
    receiver = new User("testUser", "test@example.com", "password", null);
    ReflectionTestUtils.setField(receiver, "id", receiverId);

    notificationId = UUID.randomUUID();
    notification = new Notification(receiver, "제목", "내용");
    ReflectionTestUtils.setField(notification, "id", notificationId);
    ReflectionTestUtils.setField(notification, "createdAt", Instant.now());

    notificationDto = new NotificationDto(notificationId, notification.getCreatedAt(), receiverId,
        "제목", "내용");
  }

  @Test
  @DisplayName("알림 생성 성공")
  void createNotification_Success() {
    // given
    given(userRepository.findById(eq(receiverId))).willReturn(Optional.of(receiver));
    given(notificationMapper.toDto(any(Notification.class))).willReturn(notificationDto);

    // when
    NotificationDto result = notificationService.create(receiverId, "제목", "내용");

    // then
    assertThat(result).isEqualTo(notificationDto);
    verify(notificationRepository).save(any(Notification.class));
  }

  @Test
  @DisplayName("존재하지 않는 수신자로 알림 생성 시도 시 실패")
  void createNotification_WithNonExistentReceiver_ThrowsException() {
    // given
    given(userRepository.findById(eq(receiverId))).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> notificationService.create(receiverId, "제목", "내용"))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  @DisplayName("알림 조회 성공")
  void findNotification_Success() {
    // given
    given(notificationRepository.findById(eq(notificationId))).willReturn(Optional.of(notification));
    given(notificationMapper.toDto(eq(notification))).willReturn(notificationDto);

    // when
    NotificationDto result = notificationService.find(notificationId);

    // then
    assertThat(result).isEqualTo(notificationDto);
  }

  @Test
  @DisplayName("존재하지 않는 알림 조회 시 예외 발생")
  void findNotification_WithNonExistentId_ThrowsException() {
    // given
    given(notificationRepository.findById(eq(notificationId))).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> notificationService.find(notificationId))
        .isInstanceOf(NotificationNotFoundException.class);
  }

  @Test
  @DisplayName("수신자별 알림 목록 조회 성공")
  void findAllByReceiverId_Success() {
    // given
    given(notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(eq(receiverId)))
        .willReturn(List.of(notification));
    given(notificationMapper.toDto(eq(notification))).willReturn(notificationDto);

    // when
    List<NotificationDto> result = notificationService.findAllByReceiverId(receiverId);

    // then
    assertThat(result).containsExactly(notificationDto);
  }

  @Test
  @DisplayName("알림 삭제(확인) 성공")
  void deleteNotification_Success() {
    // given
    given(notificationRepository.existsById(eq(notificationId))).willReturn(true);

    // when
    notificationService.delete(notificationId);

    // then
    verify(notificationRepository).deleteById(eq(notificationId));
  }

  @Test
  @DisplayName("존재하지 않는 알림 삭제 시도 시 실패")
  void deleteNotification_WithNonExistentId_ThrowsException() {
    // given
    given(notificationRepository.existsById(eq(notificationId))).willReturn(false);

    // when & then
    assertThatThrownBy(() -> notificationService.delete(notificationId))
        .isInstanceOf(NotificationNotFoundException.class);
  }
}

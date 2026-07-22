package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.exception.notification.NotificationForbiddenException;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
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
  private NotificationMapper notificationMapper;

  @InjectMocks
  private BasicNotificationService notificationService;

  private UUID receiverId;
  private UUID notificationId;
  private Notification notification;
  private NotificationDto notificationDto;

  @BeforeEach
  void setUp() {
    receiverId = UUID.randomUUID();
    notificationId = UUID.randomUUID();

    notification = new Notification(receiverId, "테스트 제목", "테스트 내용");
    ReflectionTestUtils.setField(notification, "id", notificationId);
    ReflectionTestUtils.setField(notification, "createdAt", Instant.now());

    notificationDto = new NotificationDto(notificationId, receiverId, "테스트 제목", "테스트 내용", Instant.now());
  }

  @Test
  @DisplayName("알림 생성 성공")
  void create_Success() {
    // given
    given(notificationRepository.save(any(Notification.class))).willReturn(notification);
    given(notificationMapper.toDto(notification)).willReturn(notificationDto);

    // when
    NotificationDto result = notificationService.create(receiverId, "테스트 제목", "테스트 내용");

    // then
    assertThat(result.receiverId()).isEqualTo(receiverId);
    assertThat(result.title()).isEqualTo("테스트 제목");
    assertThat(result.content()).isEqualTo("테스트 내용");
    verify(notificationRepository).save(any(Notification.class));
  }

  @Test
  @DisplayName("수신자 ID로 알림 목록 조회 성공")
  void findAllByReceiverId_Success() {
    // given
    Notification notification2 = new Notification(receiverId, "두번째 알림", "내용2");
    ReflectionTestUtils.setField(notification2, "id", UUID.randomUUID());
    NotificationDto dto2 = new NotificationDto(UUID.randomUUID(), receiverId, "두번째 알림", "내용2", Instant.now());

    given(notificationRepository.findAllByReceiverId(receiverId))
        .willReturn(List.of(notification, notification2));
    given(notificationMapper.toDto(notification)).willReturn(notificationDto);
    given(notificationMapper.toDto(notification2)).willReturn(dto2);

    // when
    List<NotificationDto> result = notificationService.findAllByReceiverId(receiverId);

    // then
    assertThat(result).hasSize(2);
    assertThat(result).allMatch(dto -> dto.receiverId().equals(receiverId));
  }

  @Test
  @DisplayName("알림이 없는 수신자 조회 시 빈 목록 반환")
  void findAllByReceiverId_Empty() {
    // given
    given(notificationRepository.findAllByReceiverId(receiverId)).willReturn(List.of());

    // when
    List<NotificationDto> result = notificationService.findAllByReceiverId(receiverId);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("본인 알림 삭제 성공")
  void delete_Success() {
    // given
    given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

    // when
    notificationService.delete(notificationId, receiverId);

    // then
    verify(notificationRepository).deleteById(notificationId);
  }

  @Test
  @DisplayName("타인의 알림 삭제 시 403 예외 발생")
  void delete_Forbidden_WhenNotOwner() {
    // given
    UUID anotherUserId = UUID.randomUUID();
    given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

    // when & then
    assertThatThrownBy(() -> notificationService.delete(notificationId, anotherUserId))
        .isInstanceOf(NotificationForbiddenException.class);
  }

  @Test
  @DisplayName("존재하지 않는 알림 삭제 시 404 예외 발생")
  void delete_NotFound() {
    // given
    given(notificationRepository.findById(notificationId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> notificationService.delete(notificationId, receiverId))
        .isInstanceOf(NotificationNotFoundException.class);
  }
}

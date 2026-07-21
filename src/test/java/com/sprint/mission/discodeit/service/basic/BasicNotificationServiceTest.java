package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sprint.mission.discodeit.config.CacheConfig;
import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BasicNotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private NotificationMapper notificationMapper;

  @Mock
  private CacheManager cacheManager;

  @Mock
  private Cache cache;

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
  @DisplayName("알림 생성 성공 - 수신자 각각에게 알림이 배치 저장되고 캐시가 무효화된다")
  void createNotification_Success() {
    // given
    given(userRepository.findAllById(Set.of(receiverId))).willReturn(List.of(receiver));
    given(notificationMapper.toDto(any(Notification.class))).willReturn(notificationDto);
    given(cacheManager.getCache(CacheConfig.NOTIFICATIONS_BY_USER_CACHE)).willReturn(cache);

    // when
    List<NotificationDto> result = notificationService.create(Set.of(receiverId), "제목", "내용");

    // then
    assertThat(result).containsExactly(notificationDto);
    verify(notificationRepository).saveAll(anyList());
    verify(cache).evict(receiverId);
  }

  @Test
  @DisplayName("수신자 집합이 비어있으면 아무 것도 하지 않는다")
  void createNotification_WithEmptyReceiverIds_DoesNothing() {
    // when
    List<NotificationDto> result = notificationService.create(Set.of(), "제목", "내용");

    // then
    assertThat(result).isEmpty();
    verify(notificationRepository, never()).saveAll(any());
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

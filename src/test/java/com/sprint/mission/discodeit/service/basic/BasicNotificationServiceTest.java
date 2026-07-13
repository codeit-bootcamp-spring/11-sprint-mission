package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
public class BasicNotificationServiceTest {

  @InjectMocks
  private BasicNotificationService notificationService;

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private CacheManager cacheManager;

  @Test
  @DisplayName("수신자 ID로 알림 목록 조회 성공")
  void findAllByReceiverId_success() {
    UUID receiverId = UUID.randomUUID();
    User receiver = new User("tester", "test@test.com", "pw");
    Notification notification = new Notification(receiver, "알림 제목", "알림 내용");

    given(notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(receiverId))
        .willReturn(List.of(notification));

    List<NotificationDto> results = notificationService.findAllByReceiverId(receiverId);

    assertThat(results).hasSize(1);
    assertThat(results.get(0).title()).isEqualTo("알림 제목");
    assertThat(results.get(0).content()).isEqualTo("알림 내용");
  }

  @Test
  @DisplayName("알림 삭제 성공 및 캐시 무효화")
  void delete_success() {
    UUID notificationId = UUID.randomUUID();
    User receiver = new User("tester", "test@test.com", "pw");
    Notification notification = new Notification(receiver, "제목", "내용");
    Cache mockCache = mock(Cache.class);

    given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));
    given(cacheManager.getCache("notifications")).willReturn(mockCache);

    notificationService.delete(notificationId);

    then(notificationRepository).should().delete(notification);
    then(mockCache).should().evict(receiver.getId());
  }

  @Test
  @DisplayName("알림 삭제 실패 - 존재하지 않는 알림")
  void delete_fail_notFound() {
    UUID notificationId = UUID.randomUUID();

    given(notificationRepository.findById(notificationId)).willReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> {
      notificationService.delete(notificationId);
    });
  }

  @Test
  @DisplayName("관리자 알림 발송 성공")
  void notifyAdmins_success() {
    User admin1 = new User("admin1", "admin1@test.com", "pw");
    admin1.updateRole(Role.ADMIN);
    User admin2 = new User("admin2", "admin2@test.com", "pw");
    admin2.updateRole(Role.ADMIN);

    given(userRepository.findByRole(Role.ADMIN)).willReturn(List.of(admin1, admin2));

    notificationService.notifyAdmins("공지사항", "서버 점검 안내");

    then(notificationRepository).should().saveAll(any());
  }

  @Test
  @DisplayName("관리자 알림 발송 중단 - 관리자 없음")
  void notifyAdmins_emptyAdmins() {
    given(userRepository.findByRole(Role.ADMIN)).willReturn(List.of());

    notificationService.notifyAdmins("공지사항", "서버 점검 안내");

    then(notificationRepository).should(never()).saveAll(any());
  }
}
package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.User.Role;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

  private final ReadStatusRepository readStatusRepository;
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  private final CacheManager cacheManager;

  @Async("eventTaskExecutor")
  // phase 생략 시 default는 AFTER_COMMIT
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {

    // 알림이 활성화된 ChannelId로 ReadStatus를 조회
    List<ReadStatus> readStatuses =
        readStatusRepository.findByChannelIdAndNotificationEnabledTrue(event.channelId());

    for (ReadStatus readStatus : readStatuses) {

      // 메시지 작성자는 제외
      if (readStatus.getUser().getId().equals(event.authorId())) {
        continue;
      }

      // 알림 생성
      notificationService.create(
          readStatus.getUser().getId(),
          event.authorUsername() + " #(" + event.channelName() + ")",
          event.content()
      );

      // 캐시 조회 후 해당 채널 참여자의 알림 캐시만 무효화
      Cache cache = cacheManager.getCache("userNotifications");

      if (cache != null) {
        cache.evict(readStatus.getUser().getId());
      }
    }
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {

    User user = userRepository.findById(event.userId()).orElseThrow(
        () -> new UserNotFoundException(event.userId())
    );

    notificationService.create(
        user.getId(),
        "권한이 변경되었습니다.",
        event.beforeRole() + " -> " + event.newRole()
    );

    // 캐시 조회 후 해당 사용자의 알림 캐시만 무효화
    Cache cache = cacheManager.getCache("userNotifications");
    if (cache != null) {
      cache.evict(user.getId());
    }
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) {

    // 관리자가 2명 이상일수도 있기 때문에 find가 아닌 findAll을 사용(List 타입)
    List<User> admins = userRepository.findAllByRole(Role.ADMIN);

    // 관리자가 없을 때
    if (admins.isEmpty()) {
      log.error("관리자 계정이 존재하지 않습니다. error={}", event.e().getMessage());
      return;
    }

    // 알림 메시지 제목
    String title = "S3 파일 업로드 실패";

    String requestId = MDC.get("requestId");

    // 알림 메시지 포맷
    String message = """
        RequestId: %s
        BinaryContentId: %s
        Error: %s
        """.formatted(requestId, event.binaryContentId(), event.e().getMessage());

    // List<User> → List<Notification>
    List<Notification> notifications = admins.stream()
        .map(admin -> Notification.create(admin, title, message))
        .toList();

    notificationRepository.saveAll(notifications);

    Cache cache = cacheManager.getCache("userNotifications");
    if (cache != null) {
      admins.forEach(admin -> cache.evict(admin.getId()));
    }
  }

}

package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.notification.NotificationCreatedEvent;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final NotificationMapper notificationMapper;

  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  @CacheEvict(value = "userNotifications", key = "#receiverId")
  public NotificationDto create(UUID receiverId, String title, String content) {

    User receiver = userRepository.findById(receiverId).orElseThrow(
        () -> new UserNotFoundException(receiverId));

    Notification notification = notificationRepository.save(
        Notification.create(receiver, title, content));

    NotificationDto dto = notificationMapper.toDto(notification);

    eventPublisher.publishEvent(
        new NotificationCreatedEvent(
            dto,
            dto.createdAt()
        )
    );

    return dto;
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "userNotifications", key = "#receiverId")
  public List<NotificationDto> findAll(UUID receiverId) {

    // receiverId에 대한 전체 알림 목록 조회
    // Stream<Notification> → map()을 통하여 Stream<NotificationDto>로 변환
    // 이후 toList()를 통해 List<NotifiationDto>로 수집(변환)
    return notificationRepository.findAllByReceiverId(receiverId).stream()
        .map(notificationMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  // userId 키에 해당하는 userNotifications 캐시 데이터만 무효화
  @CacheEvict(value = "userNotifications", key = "#userId")
  public void delete(UUID notificationId, UUID userId) {

    // notificationId에 해당하는 Notification 객체를 가져옴
    Notification notification = notificationRepository.findById(notificationId).orElseThrow(
        () -> new NotificationNotFoundException(notificationId)
    );

    // Notification 객체 내의 receiverId가 userId가 아닐 경우 403 예외 반환
    if (!notification.getReceiver().getId().equals(userId)) {
      throw new AccessDeniedException("자신의 알림만 삭제할 수 있습니다.");
    }

    // 가져온 Notification 객체를 삭제시킴
    notificationRepository.delete(notification);
  }

//  @Override
//  @Transactional
//  public void notifyAdminOfS3PutFailure(UUID binaryContentId, Exception e) {
//
//    // 관리자가 2명 이상일수도 있기 때문에 find가 아닌 findAll을 사용(List 타입)
//    List<User> admins = userRepository.findAllByRole(Role.ADMIN);
//
//    // 관리자가 없을 때
//    if (admins.isEmpty()) {
//      log.error("관리자 계정이 존재하지 않습니다. error={}", e.getMessage());
//      return;
//    }
//
//    // 알림 메시지 제목
//    String title = "S3 파일 업로드 실패";
//
//    String requestId = MDC.get("requestId");
//
//    // 알림 메시지 포맷
//    String message = """
//        RequestId: %s
//        BinaryContentId: %s
//        Error: %s
//        """.formatted(requestId, binaryContentId, e.getMessage());
//
//    // List<User> → List<Notification>
//    List<Notification> notifications = admins.stream()
//        .map(admin -> Notification.create(admin, title, message))
//        .toList();
//
//    notificationRepository.saveAll(notifications);
//
//    Cache cache = cacheManager.getCache("userNotifications");
//    if (cache != null) {
//      admins.forEach(admin -> cache.evict(admin.getId()));
//    }
//  }

}

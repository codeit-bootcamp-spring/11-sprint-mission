package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final NotificationMapper notificationMapper;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public void createAll(MessageCreatedEvent event) {
    log.debug("메시지 알림 생성 시작: messageId={}, channelId={}", event.messageId(), event.channelId());

    String title = "%s (#%s)".formatted(event.authorName(), event.channelName());
    List<Notification> notifications = readStatusRepository
        .findAllByChannelIdAndNotificationEnabledTrue(event.channelId()).stream()
        .map(ReadStatus::getUser)
        .filter(receiver -> !receiver.getId().equals(event.authorId()))
        .map(receiver -> new Notification(receiver, title, event.content()))
        .toList();
    notificationRepository.saveAll(notifications);

    log.info("메시지 알림 생성 완료: messageId={}, 생성된 알림 수={}",
        event.messageId(), notifications.size());
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public void create(RoleUpdatedEvent event) {
    log.debug("권한 변경 알림 생성 시작: userId={}", event.userId());

    UUID receiverId = event.userId();
    User receiver = userRepository.findById(receiverId)
        .orElseThrow(() -> UserNotFoundException.withId(receiverId));
    Notification notification = new Notification(
        receiver,
        "권한이 변경되었습니다.",
        "%s -> %s".formatted(event.previousRole(), event.newRole())
    );
    notificationRepository.save(notification);

    log.info("권한 변경 알림 생성 완료: id={}, userId={}", notification.getId(), receiverId);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public void createAll(S3UploadFailedEvent event) {
    log.debug("업로드 실패 알림 생성 시작: binaryContentId={}", event.binaryContentId());

    String content = """
        Task: %s
        RequestId: %s
        BinaryContentId: %s
        Error: %s""".formatted(
        event.taskName(), event.requestId(), event.binaryContentId(), event.errorMessage());
    List<Notification> notifications = userRepository.findAllByRole(Role.ADMIN).stream()
        .map(admin -> new Notification(admin, "파일 업로드에 실패했습니다.", content))
        .toList();
    notificationRepository.saveAll(notifications);

    log.info("업로드 실패 알림 생성 완료: binaryContentId={}, 생성된 알림 수={}",
        event.binaryContentId(), notifications.size());
  }

  @Transactional(readOnly = true)
  @Override
  public NotificationDto find(UUID notificationId) {
    log.debug("알림 조회 시작: id={}", notificationId);
    NotificationDto dto = notificationRepository.findById(notificationId)
        .map(notificationMapper::toDto)
        .orElseThrow(() -> NotificationNotFoundException.withId(notificationId));
    log.info("알림 조회 완료: id={}", notificationId);
    return dto;
  }

  @Transactional(readOnly = true)
  @Override
  public List<NotificationDto> findAllByReceiverId(UUID receiverId) {
    log.debug("사용자별 알림 목록 조회 시작: receiverId={}", receiverId);
    List<NotificationDto> dtos = notificationRepository.findAllByReceiverId(receiverId).stream()
        .map(notificationMapper::toDto)
        .toList();
    log.info("사용자별 알림 목록 조회 완료: receiverId={}, 조회된 항목 수={}", receiverId, dtos.size());
    return dtos;
  }

  @PreAuthorize("principal.userDto.id == @basicNotificationService.find(#notificationId).receiverId")
  @Transactional
  @Override
  public void delete(UUID notificationId) {
    log.debug("알림 삭제 시작: id={}", notificationId);
    if (!notificationRepository.existsById(notificationId)) {
      throw NotificationNotFoundException.withId(notificationId);
    }
    notificationRepository.deleteById(notificationId);
    log.info("알림 삭제 완료: id={}", notificationId);
  }
}

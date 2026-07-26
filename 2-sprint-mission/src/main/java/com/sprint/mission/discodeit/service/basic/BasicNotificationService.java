package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final CacheManager cacheManager;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Cacheable(cacheNames = "notifications", key = "#receiverId")
  public List<NotificationDto.Response> findAllByReceiverId(UUID receiverId) {
    log.debug("알림 목록 조회 시작: receiverId={}", receiverId);

    List<NotificationDto.Response> responses =
        notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(receiverId).stream()
            .map(notificationMapper::toDto)
            .toList();

    log.info("알림 목록 조회 완료: receiverId={}, 총 {}건", receiverId, responses.size());
    return responses;
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "notifications", key = "#requesterId")
  public void delete(UUID id, UUID requesterId) {
    log.debug("알림 삭제 시작: id={}", id);

    Notification notification = notificationRepository.findById(id)
        .orElseThrow(() -> NotificationNotFoundException.withId(id));

    if (!notification.getReceiverId().equals(requesterId)) {
      log.warn("타인의 알림 삭제 시도: notificationId={}, requesterId={}", id, requesterId);
      throw new AccessDeniedException("본인의 알림만 삭제할 수 있습니다.");
    }

    notificationRepository.delete(notification);
    log.info("알림 삭제 완료: id={}", id);
  }


  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createForMessage(MessageCreatedEvent event) {
    log.debug("메시지 알림 생성 시작: channelId={}", event.channelId());

    List<Notification> notifications = readStatusRepository
        .findAllByChannelIdAndNotificationEnabledTrue(event.channelId()).stream()
        .map(readStatus -> readStatus.getUser().getId())
        .filter(receiverId -> !receiverId.equals(event.authorId()))
        .map(receiverId -> Notification.builder()
            .receiverId(receiverId)
            .title(event.authorName() + " (#" + event.channelName() + ")")
            .content(event.content())
            .build())
        .toList();

    saveAndEvict(notifications);
    log.info("메시지 알림 생성 완료: channelId={}, 총 {}건", event.channelId(), notifications.size());
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createForRoleUpdate(RoleUpdatedEvent event) {
    saveAndEvict(List.of(Notification.builder()
        .receiverId(event.userId())
        .title("권한이 변경되었습니다.")
        .content(event.oldRole() + " -> " + event.newRole())
        .build()));

    log.info("권한 변경 알림 생성 완료: userId={}", event.userId());
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createForS3UploadFailure(S3UploadFailedEvent event) {
    List<Notification> notifications = userRepository.findAllByRole(Role.ADMIN).stream()
        .map(admin -> Notification.builder()
            .receiverId(admin.getId())
            .title("S3 업로드 실패")
            .content(String.format("RequestId: %s%nBinaryContentId: %s%nError: %s",
                event.requestId(), event.binaryContentId(), event.errorMessage()))
            .build())
        .toList();

    if (notifications.isEmpty()) {
      log.warn("관리자 계정이 없어 실패 알림을 생성하지 못했습니다.");
      return;
    }

    saveAndEvict(notifications);
    log.info("S3 업로드 실패 알림 생성 완료: {}건", notifications.size());
  }

  private void saveAndEvict(List<Notification> notifications) {
    if (notifications.isEmpty()) {
      return;
    }
    List<Notification> saved = notificationRepository.saveAll(notifications);

    Cache cache = cacheManager.getCache("notifications");
    if (cache != null) {
      saved.stream()
          .map(Notification::getReceiverId)
          .distinct()
          .forEach(cache::evict);
    }

    saved.stream()
        .map(notificationMapper::toDto)
        .forEach(dto -> eventPublisher.publishEvent(new NotificationCreatedEvent(dto)));
  }
}
package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.notification.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.event.dto.NotificationCreatedEvent;
import com.sprint.mission.discodeit.exception.auth.ForbiddenActionException;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
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
  private final ApplicationEventPublisher eventPublisher;

  @Cacheable(cacheNames = "notifications", key = "#receiverId")
  @Override
  public List<NotificationDto> findAllByReceiverId(UUID receiverId) {
    log.debug("알림 목록 조회 - receiverId: {}", receiverId);
    return notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(receiverId).stream()
        .map(n -> new NotificationDto(n.getId(), n.getCreatedAt(), n.getReceiverId(), n.getTitle(),
            n.getContent()))
        .toList();
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "notifications", key = "#receiverId")
  public void delete(UUID notificationId, UUID receiverId) {
    log.debug("알림 삭제 시도 - notificationId: {}, 요청 userId: {}", notificationId, receiverId);
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new NotificationNotFoundException(notificationId));

    if (!notification.getReceiverId().equals(receiverId)) {
      log.warn("알림 삭제 권한 없음 - 알림 소유자: {}, 요청자: {}", notification.getReceiverId(), receiverId);
      throw new ForbiddenActionException("해당 알림을 삭제할 권한이 없습니다.");
    }

    notificationRepository.delete(notification);
    log.info("알림 삭제 완료 - notificationId: {}", notificationId);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @CacheEvict(cacheNames = "notifications", key = "#receiverId")
  public void createNotification(UUID receiverId, String title, String content) {
    Notification notification = new Notification(receiverId, title, content);
    notificationRepository.save(notification);
    eventPublisher.publishEvent(
        new NotificationCreatedEvent(notificationMapper.toDto(notification), Instant.now()));
    log.debug("알림 생성 완료 - receiverId: {}, title: {}", receiverId, title);
  }
}
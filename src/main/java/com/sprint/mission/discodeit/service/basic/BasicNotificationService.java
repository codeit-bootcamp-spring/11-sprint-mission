package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.notification.NotificationResponse;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.exception.auth.ForbiddenException;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper mapper;

  @Transactional
  @Override
  public NotificationResponse createNotification(UUID receiverId, String title, String content) {
    log.debug("notification create trial: receiverId={}, title={}", receiverId, title);
    Notification notification = new Notification(receiverId, title, content);
    this.notificationRepository.save(notification);

    log.info("notification create success: id={}, receiverId={}", notification.getId(),
        receiverId);
    return this.mapper.toResponse(notification);
  }

  @Cacheable(cacheNames = "notifications", key = "#receiverId")
  @Override
  public List<NotificationResponse> findAllByReceiverId(UUID receiverId) {
    log.debug("notification find-all-by-receiver-id trial: receiverId={}", receiverId);
    List<Notification> notifications = this.notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(
        receiverId);

    log.info("notification find-all-by-receiver-id success: receiverId={}, count={}", receiverId,
        notifications.size());
    return notifications.stream()
        .map(this.mapper::toResponse)
        .toList();
  }

  @Transactional
  @Override
  public void deleteNotification(UUID id, UUID receiverId) {
    log.debug("notification delete trial: id={}, receiverId={}", id, receiverId);
    Notification notification = this.notificationRepository.findById(id)
        .orElseThrow(() -> NotificationNotFoundException.withId(id));

    if (!notification.getReceiverId().equals(receiverId)) {
      throw ForbiddenException.withAccessDenied();
    }

    this.notificationRepository.delete(notification);

    log.info("notification delete success: id={}", id);
  }
}
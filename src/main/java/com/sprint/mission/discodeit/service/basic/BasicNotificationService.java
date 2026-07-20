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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
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
  private final CacheManager cacheManager;

  @Transactional
  @Override
  public List<NotificationResponse> createNotification(List<UUID> receiverIds, String title,
      String content) {
    log.debug("notification create trial: receiverIds={}, title={}", receiverIds, title);
    List<Notification> notifications = receiverIds.stream()
        .map(receiverId -> new Notification(receiverId, title, content))
        .toList();
    this.notificationRepository.saveAll(notifications);

    Cache cache = this.cacheManager.getCache("notifications");
    if (cache != null) {
      receiverIds.forEach(cache::evict);
    }

    log.info("notification create success: count={}", notifications.size());
    return notifications.stream()
        .map(this.mapper::toResponse)
        .toList();
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

  @CacheEvict(cacheNames = "notifications", key = "#receiverId")
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
package com.sprint.mission.discodeit.service.basic;


import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.exception.service.common.AccessDeniedException;
import com.sprint.mission.discodeit.exception.service.notification.NonExistNotificationException;
import com.sprint.mission.discodeit.repository.JPANotificationRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicNotificationService implements NotificationService {

  private final JPANotificationRepository notificationRepository;
  private final CacheManager cacheManager;

  @Override
  @Transactional
  @CacheEvict(cacheNames = "notifications", key = "#notification.receiverId")
  public void create(UUID receiverId, String title, String content) {
    notificationRepository.save(new Notification(receiverId, title, content));
  }

  @Override
  @Cacheable(cacheNames = "notifications", key = "#receiverId")
  @Transactional(readOnly = true)
  public List<NotificationDto> findAll(UUID receiverId) {
    return notificationRepository.findAllByReceiveId(receiverId).stream()
        .map(Notification::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void delete(UUID notificationId, UUID userId) {

    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new NonExistNotificationException(notificationId));

    if (!notification.getReceiveId().equals(userId)) {
      throw new AccessDeniedException("userId", userId);
    }

    notificationRepository.deleteById(notificationId);

    UUID receiverId = notification.getReceiveId();

    Cache cache = cacheManager.getCache("notifications");
    if (cache != null) {
      cache.evict(receiverId);
    }
  }
}

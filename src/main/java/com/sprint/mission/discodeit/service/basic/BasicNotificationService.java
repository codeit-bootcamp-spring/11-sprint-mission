package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.exception.notification.NotificationForbiddenException;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;

  @CacheEvict(cacheNames = "notifications", key = "#receiverId")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public NotificationDto create(UUID receiverId, String title, String content) {
    Notification notification = notificationRepository.save(new Notification(receiverId, title, content));
    return notificationMapper.toDto(notification);
  }

  @Cacheable(cacheNames = "notifications", key = "#receiverId")
  @Transactional(readOnly = true)
  @Override
  public List<NotificationDto> findAllByReceiverId(UUID receiverId) {
    return notificationRepository.findAllByReceiverId(receiverId).stream()
        .map(notificationMapper::toDto)
        .toList();
  }

  @CacheEvict(cacheNames = "notifications", key = "#currentUserId")
  @Transactional
  @Override
  public void delete(UUID notificationId, UUID currentUserId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> NotificationNotFoundException.withId(notificationId));

    if (!notification.getReceiverId().equals(currentUserId)) {
      throw NotificationForbiddenException.forUser(currentUserId, notificationId);
    }

    notificationRepository.deleteById(notificationId);
  }
}

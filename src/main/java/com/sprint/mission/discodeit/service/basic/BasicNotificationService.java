package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.notification.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.event.NotificationCreatedEvent;
import com.sprint.mission.discodeit.exception.notification.NotificationAccessDeniedException;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final ApplicationEventPublisher eventPublisher;


  @Override
  @Transactional
  @CacheEvict(cacheNames = "notifications", key = "#receiverId")
  public NotificationDto create(UUID receiverId, String title, String content) {
    Notification notification = new Notification(receiverId, title, content);
    Notification saved = notificationRepository.save(notification);
    NotificationDto dto = new NotificationDto(saved.getId(), saved.getCreatedAt(),
        saved.getReceiverId(),
        saved.getTitle(), saved.getContent());
    eventPublisher.publishEvent(new NotificationCreatedEvent(dto, saved.getCreatedAt()));
    return dto;
  }

  @Override
  @Cacheable(cacheNames = "notifications", key = "#receiverId")
  public List<NotificationDto> findAllByReceiverId(UUID receiverId) {
    return notificationRepository.findAllByReceiverId(receiverId).stream()
        .map(n -> new NotificationDto(n.getId(), n.getCreatedAt(), n.getReceiverId(),
            n.getTitle(), n.getContent()))
        .toList();
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "notifications", key = "#requesterId")
  public void delete(UUID notificationId, UUID requesterId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new NotificationNotFoundException(notificationId));

    if (!notification.getReceiverId().equals(requesterId)) {
      throw new NotificationAccessDeniedException(notificationId, requesterId);
    }

    notificationRepository.delete(notification);
  }
}

package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.exception.NotificationAccessDeniedException;
import com.sprint.mission.discodeit.exception.NotificationNotFoundException;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;

  @Override
  @Transactional
  public NotificationDto create(UUID receiverId, String title, String content) {
    Notification notification = new Notification(receiverId, title, content);
    notificationRepository.save(notification);
    return toDto(notification);
  }

  @Override
  public List<NotificationDto> findAllByReceiverId(UUID receiverId) {
    return notificationRepository.findAllByReceiverId(receiverId).stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void delete(UUID notificationId, UUID requesterId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new NotificationNotFoundException(notificationId));
    if (!notification.getReceiverId().equals(requesterId)) {
      throw new NotificationAccessDeniedException(notificationId, requesterId);
    }
    notificationRepository.delete(notification);
  }

  private NotificationDto toDto(Notification n) {
    return new NotificationDto(n.getId(), n.getCreatedAt(), n.getReceiverId(), n.getTitle(),
        n.getContent());
  }
}

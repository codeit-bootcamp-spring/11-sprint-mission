package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;

  @Override
  public List<NotificationDto> findAllByReceiverId(UUID receiverId) {
    return notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(receiverId)
        .stream().map(n -> new NotificationDto(
            n.getId(), n.getCreatedAt(), n.getReceiver().getId(), n.getTitle(), n.getContent()
        )).toList();
  }

  @Override
  @Transactional
  @PreAuthorize("@notificationRepository.findById(#id).orElse(null)?.receiver?.id == principal.userDto.id")
  public void delete(UUID id) {
    if (!notificationRepository.existsById(id)) {
      throw new RuntimeException("알림이 존재하지 않습니다.");
    }
    notificationRepository.deleteById(id);
  }
}

package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

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

  @Override
  @Transactional
  public void notifyAdmins(String title, String content) {
    log.info("관리자 알림 발송 시작 - title: {}", title);

    List<User> admins = userRepository.findByRole(Role.ADMIN);

    if (admins.isEmpty()) {
      log.warn("등록된 관리자가 없어 알림을 발송할 수 없습니다.");
      return;
    }

    List<Notification> notifications = admins.stream()
        .map(admin -> new Notification(admin, title, content)).toList();

    notificationRepository.saveAll(notifications);

    log.info("관리자 알림 발송 완료 - 대상: {}명", admins.size());
  }
}

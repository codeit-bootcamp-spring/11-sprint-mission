package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.config.CacheConfig;
import com.sprint.mission.discodeit.dto.notification.NotificationDto;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;

  @Cacheable(cacheNames = "notifications", key = "#receiverId")
  @Override
  public List<NotificationDto> findAllByReceiverId(UUID receiverId) {
    return notificationRepository.findAllByReceiver_IdOrderByCreatedAtDesc(receiverId)
        .stream().map(notificationMapper::toDto)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Transactional
  @Override
  @PreAuthorize("@notificationAuthGuard.isOwner(#notificationId, authentication.principal.userDto.id)")
  @CacheEvict(cacheNames = CacheConfig.NOTIFICATIONS, key = "#receiverId")
  public void delete(UUID notificationId, UUID receiverId) {
    notificationRepository.deleteById(notificationId);
    log.info("알림 삭제(읽음) 완료 - notificationId: {}", notificationId);
  }

}

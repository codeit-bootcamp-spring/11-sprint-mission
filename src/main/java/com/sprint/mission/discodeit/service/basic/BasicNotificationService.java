package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.config.CacheConfig;
import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final NotificationMapper notificationMapper;
  private final CacheManager cacheManager;

  // 채널 구독자 전체 등 여러 명에게 동일한 알림을 보낼 때, 수신자 수만큼 반복 호출(N+1 insert/evict)하지
  // 않도록 saveAll로 한 번에 저장하고, 캐시도 수신자별로 정확히 evict한다(수신자 집합이 매번 달라지므로
  // 선언적 @CacheEvict로는 표현이 안 돼 CacheManager를 직접 사용).
  @Transactional
  @Override
  public List<NotificationDto> create(Set<UUID> receiverIds, String title, String content) {
    if (receiverIds.isEmpty()) {
      log.warn("알림 생성 요청이 비어있어 아무 것도 하지 않습니다.");
      return List.of();
    }
    log.debug("알림 생성 시작: receiverIds={}, title={}", receiverIds, title);

    List<User> receivers = userRepository.findAllById(receiverIds);
    List<Notification> notifications = receivers.stream()
        .map(receiver -> new Notification(receiver, title, content))
        .toList();
    notificationRepository.saveAll(notifications);

    evictNotificationCache(receiverIds);

    log.info("알림 생성 완료: receiverIds={}, 생성된 개수={}", receiverIds, notifications.size());
    return notifications.stream().map(notificationMapper::toDto).toList();
  }

  private void evictNotificationCache(Set<UUID> receiverIds) {
    Cache cache = cacheManager.getCache(CacheConfig.NOTIFICATIONS_BY_USER_CACHE);
    if (cache == null) {
      log.warn("알림 캐시가 존재하지 않아 무효화를 건너뜁니다.");
      return;
    }
    receiverIds.forEach(cache::evict);
  }

  @Transactional(readOnly = true)
  @Override
  public NotificationDto find(UUID notificationId) {
    log.debug("알림 조회 시작: id={}", notificationId);
    NotificationDto dto = notificationRepository.findById(notificationId)
        .map(notificationMapper::toDto)
        .orElseThrow(() -> NotificationNotFoundException.withId(notificationId));
    log.info("알림 조회 완료: id={}", notificationId);
    return dto;
  }

  @Cacheable(cacheNames = CacheConfig.NOTIFICATIONS_BY_USER_CACHE, key = "#receiverId")
  @Transactional(readOnly = true)
  @Override
  public List<NotificationDto> findAllByReceiverId(UUID receiverId) {
    log.debug("사용자별 알림 목록 조회 시작: receiverId={}", receiverId);
    List<NotificationDto> dtos = notificationRepository
        .findAllByReceiverIdOrderByCreatedAtDesc(receiverId).stream()
        .map(notificationMapper::toDto)
        .toList();
    log.info("사용자별 알림 목록 조회 완료: receiverId={}, 조회된 항목 수={}", receiverId, dtos.size());
    return dtos;
  }

  // 알림의 receiverId는 파라미터로 전달되지 않으므로, 삭제 전 조회 결과(@PreAuthorize와 동일한
  // 패턴으로 자기 자신 빈을 참조)를 키로 사용해 해당 사용자의 알림 목록 캐시만 정확히 무효화한다.
  @CacheEvict(
      cacheNames = CacheConfig.NOTIFICATIONS_BY_USER_CACHE,
      key = "@basicNotificationService.find(#notificationId).receiverId"
  )
  @PreAuthorize("principal.userDto.id == @basicNotificationService.find(#notificationId).receiverId")
  @Transactional
  @Override
  public void delete(UUID notificationId) {
    log.debug("알림 삭제(확인) 시작: id={}", notificationId);
    if (!notificationRepository.existsById(notificationId)) {
      throw NotificationNotFoundException.withId(notificationId);
    }
    notificationRepository.deleteById(notificationId);
    log.info("알림 삭제(확인) 완료: id={}", notificationId);
  }
}
